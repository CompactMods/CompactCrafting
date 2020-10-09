package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends ForgeRegistryEntry<MiniaturizationRecipe> {

    private IRecipeLayer[] layers;
    public Item catalyst;
    public ItemStack[] outputs;
    private AxisAlignedBB dimensions;

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private Map<String, BlockState> components;

    public MiniaturizationRecipe() {
        this.layers = new IRecipeLayer[0];
        this.outputs = new ItemStack[0];
        this.components = new HashMap<>();

        recalculateDimensions();
    }

    public void setLayers(IRecipeLayer[] layers) {
        this.layers = layers;
        this.recalculateDimensions();
    }

    private void recalculateDimensions() {
        int height = this.layers.length;
        int x = 0;
        int z = 0;

        for(IRecipeLayer layer : this.layers) {
            AxisAlignedBB dimensions = layer.getDimensions();
            if(dimensions.getXSize() > x)
                x = (int) Math.ceil(dimensions.getXSize());

            if(dimensions.getZSize() > z)
                z = (int) Math.ceil(dimensions.getZSize());
        }

        this.dimensions = new AxisAlignedBB(Vector3d.ZERO, new Vector3d(x, height, z));
    }

    public boolean addComponent(String key, BlockState block) {
        if (components.containsKey(key))
            return false;

        components.put(key, block);
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

    private boolean hasMatchingBottomLayer(IWorldReader world, FieldProjectionSize fieldSize, AxisAlignedBB field) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        return true;
    }

    public boolean matches(IWorldReader world, FieldProjectionSize fieldSize, MiniaturizationFieldBlockData fieldBlocks) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        // We know that the recipe will at least fit inside the current projection field
        AxisAlignedBB filledBounds = fieldBlocks.getFilledBounds();

        // Check rest of the recipe layers
        int maxY = (int) dimensions.getYSize();
        for(int offset = 0; offset < maxY; offset++) {
            BlockPos[] layerFilled = BlockSpaceUtil.getFilledBlocksByLayer(world, filledBounds, offset);

            boolean layerMatches = doLayerBlocksMatch(world, filledBounds, layerFilled);
            if(!layerMatches)
                return false;
        }

        return true;
    }

    public ItemStack[] getOutputs() {
        return outputs;
    }

    public Optional<BlockState> getRecipeComponent(String i) {
        if (this.components.containsKey(i)) {
            BlockState component = components.get(i);
            return Optional.of(component);
        }

        return Optional.empty();
    }

    public Optional<String> getRecipeComponentKey(BlockState state) {
        for(String comp : this.components.keySet()) {
            if(components.get(comp) == state)
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
     * Checks if a given recipe layer matches the filled blocks provided.
     *
     * @param world
     * @param fieldFilledBounds The boundaries of all filled blocks in the field.
     * @param filledPositions The filled positions on the layer to check.
     * @return
     */
    public boolean doLayerBlocksMatch(IWorldReader world, AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
        // Recipe layers using this method must define at least one filled space
        if(filledPositions.length == 0)
            return false;

        int filledYLevel = filledPositions[0].getY();
        int minFilledLevel = (int) Math.floor(fieldFilledBounds.minY);
        int yLevelRelative = filledYLevel - minFilledLevel;

        Optional<IRecipeLayer> layer = this.getLayer(yLevelRelative);

        // No such layer exists
        if(!layer.isPresent())
            return false;

        IRecipeLayer l = layer.get();

        int totalFilled = filledPositions.length;
        int requiredFilled = l.getNumberFilledPositions();

        if(totalFilled != requiredFilled)
            return false;

        BlockPos[] fieldNormalizedPositions = BlockSpaceUtil.normalizeLayerPositions(fieldFilledBounds, filledPositions);
        int extraYOffset = fieldNormalizedPositions[0].getY();

        for(BlockPos fieldFilledPosition : fieldNormalizedPositions) {
            BlockPos realPos = BlockSpaceUtil.denormalizeLayerPosition(fieldFilledBounds, fieldFilledPosition);
            BlockState state = world.getBlockState(realPos);

            // If we require a block at a position and it's air...
            BlockPos zeroedRecipePosition = fieldFilledPosition.offset(Direction.DOWN, extraYOffset);
            if(!l.isPositionRequired(zeroedRecipePosition)) {
                CompactCrafting.LOGGER.debug("Position filled but the recipe does not require a block there; recipe not matched.");
                return false;
            }

            // Position is required but the block there is air?
            // This shouldn't happen, the air check should have happened before this
            if(state.isAir(world, realPos))
                return false;

            String requiredCompKey = l.getRequiredComponentKeyForPosition(zeroedRecipePosition);
            Optional<String> realComponentInPosition = this.getRecipeComponentKey(state);

            if(requiredCompKey == null) {
                CompactCrafting.LOGGER.error("Relative position marked as required but the recipe layer did not have a lookup.");
                return false;
            }

            // No lookup defined in the recipe for the state in the position
            if(!realComponentInPosition.isPresent())
                return false;

            // Does component match in position?
            if(!realComponentInPosition.get().equals(requiredCompKey))
                return false;
        }

        return true;
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if(y < 0 || y > this.layers.length - 1)
            return Optional.empty();

        return Optional.of(this.layers[y]);
    }
}
