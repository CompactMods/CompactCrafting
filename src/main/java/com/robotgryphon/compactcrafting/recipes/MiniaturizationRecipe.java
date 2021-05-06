package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponent;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.setup.RecipeBase;
import com.robotgryphon.compactcrafting.recipes.exception.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayer;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends RecipeBase {

    /**
     * Only used for recipe dimension calculation from loading phase. Specifies the minimum field size required for fluid recipe layers.
     */
    private int minRecipeDimensions = -1;
    private ResourceLocation id;
    private int tickDuration = 200;
    private Map<Integer, IRecipeLayer> layers;
    private ItemStack catalyst = ItemStack.EMPTY;
    private final List<ItemStack> outputs;
    private AxisAlignedBB dimensions;
    private Map<String, Integer> cachedComponentTotals;

    /**
     * Contains a mapping of all known components in the recipe. Vanilla style; C = CHARCOAL_BLOCK
     */
    private final Map<String, RecipeBlockStateComponent> blockComponents;

    private static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerType.CODEC.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    private static final Codec<RecipeComponent> COMPONENT_CODEC =
            RecipeComponentType.CODEC.dispatchStable(RecipeComponent::getType, RecipeComponentType::getCodec);

    public static final Codec<MiniaturizationRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("recipeSize").forGetter(MiniaturizationRecipe::getRecipeSize),
            Codec.INT.fieldOf("tickDuration").orElse(200).forGetter(MiniaturizationRecipe::getTickDuration),
            LAYER_CODEC.listOf().fieldOf("layers").forGetter(MiniaturizationRecipe::getLayerListForCodecWrite),
            ItemStack.CODEC.fieldOf("catalyst").forGetter(MiniaturizationRecipe::getCatalyst),
            ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(MiniaturizationRecipe::getOutputs),
            Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC).fieldOf("components").forGetter(MiniaturizationRecipe::getRecipeComponents)
    ).apply(i, MiniaturizationRecipe::new));

    private Optional<Integer> getRecipeSize() {
        return this.minRecipeDimensions <= 0 ? Optional.empty() : Optional.of(this.minRecipeDimensions);
    }

    public MiniaturizationRecipe(ResourceLocation rl) {
        this.id = rl;
        this.layers = new HashMap<>();
        this.outputs = new ArrayList<>();
        this.blockComponents = new HashMap<>();

        recalculateDimensions();
    }

    public MiniaturizationRecipe(ResourceLocation id, int minRecipeDimensions, List<IRecipeLayer> layers, Map<String, RecipeBlockStateComponent> blockComponents) {
        this.id = id;
        this.minRecipeDimensions = minRecipeDimensions;
        this.layers = new HashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            this.layers.put(i, layers.get(i));
        }
        this.outputs = new ArrayList<>();
        this.blockComponents = blockComponents;

        recalculateDimensions();
    }

    public MiniaturizationRecipe(Optional<Integer> minRecipeDimensions, int tickDuration, List<IRecipeLayer> layers,
            ItemStack catalyst, List<ItemStack> outputs, Map<String, RecipeComponent> compMap) {
        this.minRecipeDimensions = minRecipeDimensions.orElse(-1);
        this.tickDuration = tickDuration;
        this.catalyst = catalyst;
        this.outputs = new ArrayList<>(outputs);

        this.layers = new HashMap<>();
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));

        this.blockComponents = new HashMap<>();
        for (Map.Entry<String, RecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if (comp.getValue() instanceof RecipeBlockStateComponent) {
                this.blockComponents.put(comp.getKey(), (RecipeBlockStateComponent) comp.getValue());
            }
        }

        this.recalculateDimensions();
    }

    private List<IRecipeLayer> getLayerListForCodecWrite() {
        ImmutableList.Builder<IRecipeLayer> l = ImmutableList.builder();
        for (int y = layers.size() - 1; y >= 0; y--)
            l.add(layers.get(y));

        return l.build();
    }

    public Map<String, RecipeComponent> getRecipeComponents() {
        Map<String, RecipeComponent> allComponents = new HashMap<>();
        allComponents.putAll(blockComponents);

        return allComponents;
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
                setFluidDimensions(new AxisAlignedBB(0, 0, 0, minRecipeDimensions, height, minRecipeDimensions));
            } catch (MiniaturizationRecipeException e) {

            }
        } else {
            for (IRecipeLayer l : this.layers.values()) {
                // We only need to worry about fixed-dimension layers; the fluid layers will adapt
                if (l instanceof IRigidRecipeLayer) {
                    AxisAlignedBB dimensions = ((IRigidRecipeLayer) l).getDimensions();
                    if (dimensions.getXsize() > x)
                        x = (int) Math.ceil(dimensions.getXsize());

                    if (dimensions.getZsize() > z)
                        z = (int) Math.ceil(dimensions.getZsize());
                }
            }

            this.dimensions = AxisAlignedBB.ofSize(x, height, z);
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

        for (Rotation rot : Rotation.values()) {
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
                BlockPos normalizedRotatedPos = BlockSpaceUtil.normalizeLayerPosition(filledBounds, rotatedPos).below(offset);

                BlockState blockState = world.getBlockState(unrotatedPos);
                for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
                    Property<?> prop = entry.getKey();
                    Comparable<?> comp = entry.getValue();
                    if (prop instanceof DirectionProperty) {
                        blockState = blockState.setValue((DirectionProperty) prop, rot.rotate((Direction) comp));
                    }
                }

                IRecipeLayer l = layer.get();
                String requiredComponentKeyForPosition = l.getRequiredComponentKeyForPosition(normalizedRotatedPos).get();
                Optional<String> recipeComponentKey = this.getRecipeComponentKey(blockState);

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

    public List<ItemStack> getOutputs() {
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

    public Optional<RecipeBlockStateComponent> getRecipeBlockComponent(String i) {
        if (this.blockComponents.containsKey(i)) {
            RecipeBlockStateComponent component = blockComponents.get(i);
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

            Map<String, Integer> layerTotals = layer.getComponentTotals(this.blockComponents);

            if (layerTotals.containsKey(i))
                required += layerTotals.get(i);
        }

        return required;
    }

    public Optional<String> getRecipeComponentKey(BlockState state) {
        // TODO - This might conflict with multiple matching states, consider handling this in the codec loading process
        for (String comp : this.blockComponents.keySet()) {
            RecipeBlockStateComponent sComp = blockComponents.get(comp);
            if (sComp.filterMatches(state))
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
     * @param filledPositions The filled positions on the layer to check.
     * @return
     */
    public boolean areLayerPositionsCorrect(IRecipeLayer layer, AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
        // Recipe layers using this method must define at least one filled space
        if (filledPositions.length == 0)
            return false;

        int totalFilled = filledPositions.length;
        int requiredFilled = layer.getNumberFilledPositions(this.getRecipeComponents());

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

    public Stream<IRecipeLayer> getLayerStream() {
        return Arrays.stream(layers.values().toArray(new IRecipeLayer[0]).clone());
    }

    public int getNumberLayers() {
        return layers.size();
    }

    public void setLayer(int num, IRecipeLayer layer) {
        if (num < 0 || num > layers.size() - 1)
            return;

        this.layers.put(num, layer);
        this.postLayerChange();
    }

    public Set<String> getComponentKeys() {
        return this.blockComponents.keySet();
    }

    public void addOutput(ItemStack itemStack) {
        this.outputs.add(itemStack);
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

    public int getTickDuration() {
        return this.tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
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
