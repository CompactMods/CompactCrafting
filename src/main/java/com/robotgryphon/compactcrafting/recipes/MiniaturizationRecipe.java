package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.data.base.RecipeBase;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
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

    private ResourceLocation registryName;
    private IRecipeLayer[] layers;
    private ItemStack catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;
    private Map<String, Integer> cachedComponentTotals;
    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private final Map<String, BlockState> components;

    public MiniaturizationRecipe(ResourceLocation rl) {
        this.registryName = rl;
        this.layers = new IRecipeLayer[0];
        this.outputs = new ItemStack[0];
        this.components = new HashMap<>();

        recalculateDimensions();
    }

    public void setLayers(IRecipeLayer[] layers) {
        this.layers = layers;
        this.postLayerChange();
    }

    private void postLayerChange() {
        this.cachedComponentTotals = null;
        this.recalculateDimensions();
    }

    private void recalculateDimensions() {
        int height = this.layers.length;
        int x = 0;
        int z = 0;

        for (IRecipeLayer layer : this.layers) {
            // We only need to worry about fixed-dimension layers; the fluid layers will adapt
            if (layer instanceof IRigidRecipeLayer) {
                AxisAlignedBB dimensions = ((IRigidRecipeLayer) layer).getDimensions();
                if (dimensions.getXSize() > x)
                    x = (int) Math.ceil(dimensions.getXSize());

                if (dimensions.getZSize() > z)
                    z = (int) Math.ceil(dimensions.getZSize());
            }
        }

        this.dimensions = new AxisAlignedBB(Vector3d.ZERO, new Vector3d(x, height, z));

        updateFluidLayerDimensions();
    }

    private void updateFluidLayerDimensions() {
        // Update all the dynamic recipe layers
        Arrays.stream(this.layers)
                .filter(layer -> layer instanceof IDynamicRecipeLayer)
                .forEach(dynamicLayer -> {
                    ((IDynamicRecipeLayer) dynamicLayer).setRecipeDimensions(dimensions);
                });
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
                BlockPos normalizedRotatedPos = BlockSpaceUtil.normalizeLayerPosition(filledBounds, rotatedPos).down(offset);

                BlockState actualState = world.getBlockState(unrotatedPos);

                IRecipeLayer l = layer.get();
                String requiredComponentKeyForPosition = l.getRequiredComponentKeyForPosition(normalizedRotatedPos);
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
        if(this.cachedComponentTotals != null)
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
        for (IRecipeLayer layer : this.layers) {
            if(layer == null)
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
                .map(p -> p.offset(Direction.DOWN, extraYOffset))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        return Arrays.stream(fieldNormalizedPositionsLayerOffset)
                .parallel()
                .allMatch(layer::isPositionRequired);
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if (y < 0 || y > this.layers.length - 1)
            return Optional.empty();

        return Optional.ofNullable(this.layers[y]);
    }

    public Stream<IRecipeLayer> getLayers() {
        return Arrays.stream(layers.clone());
    }

    public int getNumberLayers() {
        return layers.length;
    }

    public void setLayer(int num, IRecipeLayer layer) {
        if(num < 0 || num > layers.length - 1)
            return;

        this.layers[num] = layer;
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
        if(Arrays.stream(this.layers).filter(Objects::nonNull).anyMatch(layer -> !(layer instanceof IDynamicRecipeLayer)))
            throw new MiniaturizationRecipeException("Tried to set fluid dimensions when a non-fluid layer exists.");

        this.dimensions = dimensions;
        updateFluidLayerDimensions();
    }

    public int getTicks() {
        return 1;
    }

    @Override
    public ResourceLocation getId() {
        return this.registryName;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registration.MINIATURIZATION_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registration.MINIATURIZATION_RECIPE_TYPE;
    }
}
