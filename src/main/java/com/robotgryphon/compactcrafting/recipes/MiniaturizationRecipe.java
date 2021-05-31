package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.api.components.IRecipeBlockComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponent;
import com.robotgryphon.compactcrafting.api.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.api.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.api.layers.dim.IDynamicSizedRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.dim.IFixedSizedRecipeLayer;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponentTypeCodec;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerBlocks;
import com.robotgryphon.compactcrafting.recipes.setup.RecipeBase;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends RecipeBase {

    /**
     * Only used for recipe dimension calculation from loading phase.
     * Specifies the minimum field size required for fluid recipe layers.
     */
    private int minRecipeDimensions;
    private ResourceLocation id;
    private Map<Integer, IRecipeLayer> layers;
    private ItemStack catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;
    private Map<String, Integer> cachedComponentTotals;

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private Map<String, IRecipeBlockComponent> blockComponents;

    /**
     * Contains a mapping of non-block components in the recipe.
     * To be used for future expansion.
     */
    private Map<String, IRecipeComponent> otherComponents;

    private static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerType.CODEC.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    private static final Codec<IRecipeComponent> COMPONENT_CODEC =
            RecipeComponentTypeCodec.INSTANCE.dispatchStable(IRecipeComponent::getType, RecipeComponentType::getCodec);

    public static final Codec<MiniaturizationRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("recipeSize").forGetter(x -> x.minRecipeDimensions),
            LAYER_CODEC.listOf().fieldOf("layers").forGetter(MiniaturizationRecipe::getLayerListForCodecWrite),
            ItemStack.CODEC.fieldOf("catalyst").forGetter(MiniaturizationRecipe::getCatalyst),
            ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(MiniaturizationRecipe::getOutputList),
            Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC).fieldOf("components").forGetter(MiniaturizationRecipe::getComponentsForCodecWrite)
    ).apply(i, MiniaturizationRecipe::new));

    public MiniaturizationRecipe(int minRecipeDimensions, List<IRecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs,
                                 Map<String, IRecipeComponent> compMap) {
        this.minRecipeDimensions = minRecipeDimensions;
        this.catalyst = catalyst;
        this.outputs = outputs.toArray(new ItemStack[0]);

        applyLayers(layers);
        applyComponents(compMap);

        this.recalculateDimensions();
    }

    private void applyComponents(Map<String, IRecipeComponent> compMap) {
        final Map<String, IRecipeBlockComponent> blockComponents;
        this.blockComponents = new HashMap<>();
        this.otherComponents = new HashMap<>();
        for (Map.Entry<String, IRecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if(comp.getValue() instanceof IRecipeBlockComponent) {
                this.blockComponents.put(comp.getKey(), (IRecipeBlockComponent) comp.getValue());
                continue;
            }

            this.otherComponents.put(comp.getKey(), comp.getValue());
        }
    }

    private void applyLayers(List<IRecipeLayer> layers) {
        this.layers = new HashMap<>();
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));
    }

    private ImmutableList<ItemStack> getOutputList() {
        return ImmutableList.copyOf(outputs.clone());
    }

    private List<IRecipeLayer> getLayerListForCodecWrite() {
        ImmutableList.Builder<IRecipeLayer> l = ImmutableList.builder();
        for (int y = layers.size() - 1; y >= 0; y--)
            l.add(layers.get(y));

        return l.build();
    }

    private Map<String, IRecipeComponent> getComponentsForCodecWrite() {
        Map<String, IRecipeComponent> allComponents = new HashMap<>();
        allComponents.putAll(blockComponents);

        return allComponents;
    }

    private void recalculateDimensions() {
        int height = this.layers.size();
        int x = 0;
        int z = 0;

        boolean hasAnyRigidLayers = this.layers.values().stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        if (!hasAnyRigidLayers) {
            try {
                setFluidDimensions(new AxisAlignedBB(0, 0, 0, minRecipeDimensions, height, minRecipeDimensions));
            } catch (MiniaturizationRecipeException e) {

            }
        } else {
            for (IRecipeLayer l : this.layers.values()) {
                // We only need to worry about fixed-dimension layers; the fluid layers will adapt
                if (l instanceof IFixedSizedRecipeLayer) {
                    AxisAlignedBB dimensions = ((IFixedSizedRecipeLayer) l).getDimensions();
                    if (dimensions.getXsize() > x)
                        x = (int) Math.ceil(dimensions.getXsize());

                    if (dimensions.getZsize() > z)
                        z = (int) Math.ceil(dimensions.getZsize());
                }
            }

            this.dimensions = new AxisAlignedBB(Vector3d.ZERO, new Vector3d(x, height, z));
        }

        updateFluidLayerDimensions();
    }

    private void updateFluidLayerDimensions() {
        // Update all the dynamic recipe layers
        this.layers.values()
                .stream()
                .filter(l -> l instanceof IDynamicSizedRecipeLayer)
                .forEach(dl -> ((IDynamicSizedRecipeLayer) dl).setRecipeDimensions(dimensions));
    }

    /**
     * Checks that a given field size can contain this recipe.
     *
     * @param fieldSize
     * @return
     */
    public boolean fitsInFieldSize(FieldProjectionSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXsize(), dimensions.getYsize(), dimensions.getZsize())
                .allMatch(size -> size <= dim);

        return fits;
    }

    public boolean matches(IWorldReader world, FieldProjectionSize fieldSize, MiniaturizationFieldBlockData fieldBlocks) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        // We know that the recipe will at least fit inside the current projection field
        AxisAlignedBB filledBounds = fieldBlocks.getFilledBounds();

        Rotation[] validRotations = new Rotation[]{
                Rotation.NONE,
                Rotation.CLOCKWISE_90,
                Rotation.CLOCKWISE_180,
                Rotation.COUNTERCLOCKWISE_90
        };

        for (Rotation rot : validRotations) {
            boolean matchesRot = checkRotation(world, rot, filledBounds);
            if (matchesRot)
                return true;
        }

        return false;
    }

    private boolean checkRotation(IWorldReader world, Rotation rot, AxisAlignedBB filledBounds) {
        // Check the recipe layer by layer

        int maxY = (int) dimensions.getYsize();
        for (int offset = 0; offset < maxY; offset++) {
            Optional<IRecipeLayer> layer = getLayer(offset);

            // If we have no layer definition do lighter processing
            // TODO: Consider changing the layers to a map so we can make air layers null/nonexistent
            if (!layer.isPresent()) {
                // We're being safe here - if there's no layer definition we assume the layer is all-air
                return false;
            }

            AxisAlignedBB bounds = BlockSpaceUtil.getLayerBoundsByYOffset(filledBounds, offset);
            IRecipeLayerBlocks blocks = RecipeLayerBlocks.create(world, this, bounds);

            if(rot != Rotation.NONE)
                blocks = blocks.rotate(rot);

            IRecipeLayer targetLayer = layer.get();

            boolean layerMatched = targetLayer.matches(blocks);

            if(!layerMatched)
                return false;

            // TODO - Impl
            // Check that the rotated positions are correct
//            boolean layerMatches = areLayerPositionsCorrect(layer.get(), filledBounds, layerRotated.values().toArray(new BlockPos[0]));
//            if (!layerMatches)
//                return false;
//
//            // Check the states are correct
//            for (BlockPos unrotatedPos : layerFilled) {
//                BlockPos rotatedPos = layerRotated.get(unrotatedPos);
//                BlockPos normalizedRotatedPos = BlockSpaceUtil.normalizeLayerPosition(filledBounds, rotatedPos).below(offset);
//
//                BlockState actualState = world.getBlockState(unrotatedPos);
//
//                IRecipeLayer l = layer.get();
//                String requiredComponentKeyForPosition = l.getRequiredComponentKeyForPosition(normalizedRotatedPos).get();
//                Optional<String> recipeComponentKey = this.getRecipeComponentKey(actualState);
//
//                if (!recipeComponentKey.isPresent()) {
//                    // At this point we don't have a lookup for the state that's at the position
//                    // No match can be made here
//                    return false;
//                }
//
//                boolean statesEqual = recipeComponentKey.get().equals(requiredComponentKeyForPosition);
//                if (!statesEqual)
//                    return false;
//            }
        }

        return true;
    }

    public ItemStack[] getOutputs() {
        return outputs;
    }

    public Map<String, Integer> getRecipeComponentTotals() {
        if (this.cachedComponentTotals != null)
            return this.cachedComponentTotals;

        HashMap<String, Integer> totals = new HashMap<>();
        this.blockComponents.keySet().forEach(comp -> {
            int count = this.getComponentRequiredCount(comp);
            totals.put(comp, count);
        });

        this.cachedComponentTotals = totals;
        return totals;
    }

    public Optional<IRecipeBlockComponent> getRecipeBlockComponent(String i) {
        if (this.blockComponents.containsKey(i)) {
            IRecipeBlockComponent component = blockComponents.get(i);
            return Optional.of(component);
        }

        return Optional.empty();
    }

    public int getComponentRequiredCount(String i) {
        if (!this.blockComponents.containsKey(i))
            return 0;

        int required = 0;
        for (IRecipeLayer layer : this.layers.values()) {
            if (layer == null)
                continue;

            Map<String, Integer> layerTotals = layer.getComponentTotals();

            if (layerTotals.containsKey(i))
                required += layerTotals.get(i);
        }

        return required;
    }

    public Optional<String> getRecipeComponentKey(BlockState state) {
        // TODO - This might conflict with multiple matching states, consider handling this in the codec loading process
        for (String comp : this.blockComponents.keySet()) {
            IRecipeBlockComponent sComp = blockComponents.get(comp);
            if(sComp.matches(state))
                return Optional.of(comp);
        }

        return Optional.empty();
    }

    public boolean fitsInDimensions(AxisAlignedBB bounds) {
        return BlockSpaceUtil.boundsFitsInside(this.dimensions, bounds);
    }

    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    /**
     * Checks if a given recipe layer matches all the positions for a rotation.
     *
     * @param fieldFilledBounds The boundaries of all filled blocks in the field.
     * @param filledPositions   The filled positions on the layer to check.
     * @return
     */
    public boolean areLayerPositionsCorrect(IRecipeLayer layer, AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
        // Recipe layers using this method must define at least one filled space
        if (filledPositions.length == 0)
            return false;

        int totalFilled = filledPositions.length;
        int requiredFilled = layer.getNumberFilledPositions();

        // Early exit if we don't have the correct number of blocks in the layer
        if (totalFilled != requiredFilled)
            return false;

        BlockPos[] fieldNormalizedPositionsFieldOffset = BlockSpaceUtil.normalizeLayerPositions(fieldFilledBounds, filledPositions);

        int extraYOffset = fieldNormalizedPositionsFieldOffset[0].getY();

        // We'll need an extra offset layer to match against the recipe layer's Y=0
        BlockPos[] fieldNormalizedPositionsLayerOffset = Stream.of(fieldNormalizedPositionsFieldOffset)
                .parallel()
                .map(p -> p.relative(Direction.DOWN, extraYOffset))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        return Arrays.stream(fieldNormalizedPositionsLayerOffset)
                .parallel()
                .allMatch(layer::isPositionFilled);
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if (y < 0 || y > this.layers.size() - 1)
            return Optional.empty();

        return Optional.ofNullable(this.layers.get(y));
    }

    public int getNumberLayers() {
        return layers.size();
    }

    public Set<String> getComponentKeys() {
        return this.blockComponents.keySet();
    }

    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    public void setFluidDimensions(AxisAlignedBB dimensions) throws MiniaturizationRecipeException {
        boolean hasRigidLayer = this.layers.values().stream().anyMatch(layer -> layer instanceof IFixedSizedRecipeLayer);
        if (!hasRigidLayer) {
            this.dimensions = dimensions;
            updateFluidLayerDimensions();
        } else {
            CompactCrafting.LOGGER.warn("Tried to set fluid dimensions when a rigid layer is present in the layer set.", new MiniaturizationRecipeException("no. bad."));
        }
    }

    public int getTicks() {
        return 200;
    }


    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registration.MINIATURIZATION_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registration.MINIATURIZATION_RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void setId(ResourceLocation recipeId) {
        this.id = recipeId;
    }
}
