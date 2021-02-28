package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.data.base.RecipeBase;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayer;
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
    private Map<Integer, RecipeLayer> layers;
    private ItemStack catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;
    private Map<String, Integer> cachedComponentTotals;

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private final Map<String, BlockState> components;

    private static final Codec<RecipeLayer> LAYER_CODEC = RecipeLayerType.CODEC.dispatchStable(RecipeLayer::getType, RecipeLayerType::getCodec);

    public static final Codec<MiniaturizationRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("recipeSize").forGetter(MiniaturizationRecipe::getRecipeSize),
            LAYER_CODEC.listOf().fieldOf("layers").forGetter(MiniaturizationRecipe::getLayerListForCodecWrite),
            ItemStack.CODEC.fieldOf("catalyst").forGetter(MiniaturizationRecipe::getCatalyst),
            ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(MiniaturizationRecipe::getOutputList),
            Codec.unboundedMap(Codec.STRING, BlockState.CODEC).fieldOf("components").forGetter(MiniaturizationRecipe::getComponents)
    ).apply(i, MiniaturizationRecipe::new));

    private int getRecipeSize() {
        return this.minRecipeDimensions;
    }

    public MiniaturizationRecipe(ResourceLocation rl) {
        this.id = rl;
        this.layers = new HashMap<>();
        this.outputs = new ItemStack[0];
        this.components = new HashMap<>();

        recalculateDimensions();
    }

    public MiniaturizationRecipe(int minRecipeDimensions, List<RecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs, Map<String, BlockState> compMap) {
        this.minRecipeDimensions = minRecipeDimensions;
        this.catalyst = catalyst;
        this.components = compMap;
        this.outputs = outputs.toArray(new ItemStack[0]);

        this.layers = new HashMap<>();
        for (int y = 0; y < layers.size(); y++)
            this.layers.put(y, layers.get(y));

        this.recalculateDimensions();
    }

    private ImmutableList<ItemStack> getOutputList() {
        return ImmutableList.copyOf(outputs.clone());
    }

    private List<RecipeLayer> getLayerListForCodecWrite() {
        ImmutableList.Builder<RecipeLayer> l = ImmutableList.builder();
        for(int y = layers.size() - 1; y >= 0; y--)
            l.add(layers.get(y));

        return l.build();
    }

    private Map<Integer, RecipeLayer> getLayers() {
        return layers;
    }

    private void postLayerChange() {
        this.cachedComponentTotals = null;
        this.recalculateDimensions();
    }

    private void recalculateDimensions() {
        int height = this.layers.size();
        int x = 0;
        int z = 0;

        boolean hasAnyRigidLayers = this.layers.values().stream().anyMatch(l -> l instanceof IRigidRecipeLayer);
        if (!hasAnyRigidLayers) {
            try {
                setFluidDimensions(AxisAlignedBB.withSizeAtOrigin(minRecipeDimensions, height, minRecipeDimensions));
            } catch (MiniaturizationRecipeException e) {

            }
        } else {
            for (RecipeLayer l : this.layers.values()) {
                // We only need to worry about fixed-dimension layers; the fluid layers will adapt
                if (l instanceof IRigidRecipeLayer) {
                    AxisAlignedBB dimensions = ((IRigidRecipeLayer) l).getDimensions();
                    if (dimensions.getXSize() > x)
                        x = (int) Math.ceil(dimensions.getXSize());

                    if (dimensions.getZSize() > z)
                        z = (int) Math.ceil(dimensions.getZSize());
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
                .filter(l -> l instanceof IDynamicRecipeLayer)
                .forEach(dl -> ((IDynamicRecipeLayer) dl).setRecipeDimensions(dimensions));
    }

    public boolean addComponent(String key, BlockState block) {
        if (components.containsKey(key))
            return false;

        components.put(key, block);
        this.cachedComponentTotals = null;
        return true;
    }

    /**
     * Checks that a given field size can contain this recipe.
     *
     * @param fieldSize
     * @return
     */
    public boolean fitsInFieldSize(FieldProjectionSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXSize(), dimensions.getYSize(), dimensions.getZSize())
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

        int maxY = (int) dimensions.getYSize();
        for (int offset = 0; offset < maxY; offset++) {
            Optional<RecipeLayer> layer = getLayer(offset);

            BlockPos[] layerFilled = BlockSpaceUtil.getFilledBlocksByLayer(world, filledBounds, offset);

            // If we have no layer definition do lighter processing
            // TODO: Consider changing the layers to a map so we can make air layers null/nonexistent
            if (!layer.isPresent() && layerFilled.length > 0) {
                // We're being safe here - if there's no layer definition we assume the layer is all-air
                return false;
            }

            Map<BlockPos, BlockPos> layerRotated = BlockSpaceUtil.rotatePositionsInPlace(layerFilled, rot);

            // Check that the rotated positions are correct
            boolean layerMatches = areLayerPositionsCorrect(layer.get(), filledBounds, layerRotated.values().toArray(new BlockPos[0]));
            if (!layerMatches)
                return false;

            // Check the states are correct
            for (BlockPos unrotatedPos : layerFilled) {
                BlockPos rotatedPos = layerRotated.get(unrotatedPos);
                BlockPos normalizedRotatedPos = BlockSpaceUtil.normalizeLayerPosition(filledBounds, rotatedPos).down(offset);

                BlockState actualState = world.getBlockState(unrotatedPos);

                RecipeLayer l = layer.get();
                String requiredComponentKeyForPosition = l.getRequiredComponentKeyForPosition(normalizedRotatedPos).get();
                Optional<String> recipeComponentKey = this.getRecipeComponentKey(actualState);

                if (!recipeComponentKey.isPresent()) {
                    // At this point we don't have a lookup for the state that's at the position
                    // No match can be made here
                    return false;
                }

                boolean statesEqual = recipeComponentKey.get().equals(requiredComponentKeyForPosition);
                if (!statesEqual)
                    return false;
            }
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
        this.components.keySet().forEach(comp -> {
            int count = this.getComponentRequiredCount(comp);
            totals.put(comp, count);
        });

        this.cachedComponentTotals = totals;
        return totals;
    }

    public Optional<BlockState> getRecipeComponent(String i) {
        if (this.components.containsKey(i)) {
            BlockState component = components.get(i);
            return Optional.of(component);
        }

        return Optional.empty();
    }

    public int getComponentRequiredCount(String i) {
        if (!this.components.containsKey(i))
            return 0;

        int required = 0;
        for (RecipeLayer layer : this.layers.values()) {
            if (layer == null)
                continue;

            Map<String, Integer> layerTotals = layer.getComponentTotals();

            if (layerTotals.containsKey(i))
                required += layerTotals.get(i);
        }

        return required;
    }

    public Optional<String> getRecipeComponentKey(BlockState state) {
        for (String comp : this.components.keySet()) {
            if (components.get(comp) == state)
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
    public boolean areLayerPositionsCorrect(RecipeLayer layer, AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
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
                .map(p -> p.offset(Direction.DOWN, extraYOffset))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        return Arrays.stream(fieldNormalizedPositionsLayerOffset)
                .parallel()
                .allMatch(layer::isPositionFilled);
    }

    public Optional<RecipeLayer> getLayer(int y) {
        if (y < 0 || y > this.layers.size() - 1)
            return Optional.empty();

        return Optional.ofNullable(this.layers.get(y));
    }

    public Stream<RecipeLayer> getLayerStream() {
        return Arrays.stream(layers.values().toArray(new RecipeLayer[0]).clone());
    }

    public int getNumberLayers() {
        return layers.size();
    }

    public void setLayer(int num, RecipeLayer layer) {
        if (num < 0 || num > layers.size() - 1)
            return;

        this.layers.put(num, layer);
        this.postLayerChange();
    }

    public Set<String> getComponentKeys() {
        return this.components.keySet();
    }

    public void addOutput(ItemStack itemStack) {
        List<ItemStack> oTmp = new ArrayList<>(Arrays.asList(this.outputs));
        oTmp.add(itemStack);

        this.outputs = oTmp.toArray(new ItemStack[0]);
    }

    public int getNumberComponents() {
        return this.components.size();
    }

    public Map<String, BlockState> getComponents() {
        return this.components;
    }

    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    public void setCatalyst(ItemStack c) {
        this.catalyst = c;
    }

    public void setFluidDimensions(AxisAlignedBB dimensions) throws MiniaturizationRecipeException {
        boolean hasRigidLayer = this.layers.values().stream().anyMatch(layer -> layer instanceof IRigidRecipeLayer);
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
