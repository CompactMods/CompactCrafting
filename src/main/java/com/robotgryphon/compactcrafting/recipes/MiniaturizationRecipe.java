package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
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

    public boolean matches(IWorldReader world, FieldProjectionSize fieldSize, MiniaturizationFieldBlockData fieldBlocks) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        // We know that the recipe will at least fit inside the current projection field
        AxisAlignedBB filledBounds = fieldBlocks.getFilledBounds();

        Rotation[] validRotations = new Rotation[] {
                Rotation.NONE,
                Rotation.CLOCKWISE_90,
                Rotation.CLOCKWISE_180,
                Rotation.COUNTERCLOCKWISE_90
        };

        for(Rotation rot : validRotations) {
            boolean matchesRot = checkRotation(world, rot, filledBounds);
            if(matchesRot)
               return true;
        }

        return false;
    }

    private boolean checkRotation(IWorldReader world, Rotation rot, AxisAlignedBB filledBounds) {
        // Check the recipe layer by layer

        int maxY = (int) dimensions.getYSize();
        for (int offset = 0; offset < maxY; offset++) {
            BlockPos[] layerFilled = BlockSpaceUtil.getFilledBlocksByLayer(world, filledBounds, offset);
            BlockPos[] layerRotated = BlockSpaceUtil.rotatePositionsInPlace(layerFilled, rot);

            boolean layerMatches = doLayerBlocksMatch(world, rot, filledBounds, layerRotated);
            if (!layerMatches)
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
    public boolean doLayerBlocksMatch(IWorldReader world, Rotation rot, AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
        // Recipe layers using this method must define at least one filled space
        if(filledPositions.length == 0)
            return false;

        Optional<IRecipeLayer> layer = getRecipeLayerFromPositions(fieldFilledBounds, filledPositions);
        if (!layer.isPresent())
            return false;

        IRecipeLayer l = layer.get();

        int totalFilled = filledPositions.length;
        int requiredFilled = l.getNumberFilledPositions();

        // Early exit if we don't have the correct number of blocks in the layer
        if(totalFilled != requiredFilled)
            return false;

        BlockPos[] fieldNormalizedPositionsFieldOffset = BlockSpaceUtil.normalizeLayerPositions(fieldFilledBounds, filledPositions);

        // TODO: Make recipe loading respect multiple layers as Y=0, 1, etc
        int extraYOffset = fieldNormalizedPositionsFieldOffset[0].getY();

        // We'll need an extra offset layer to match against the recipe layer's Y=0
        BlockPos[] fieldNormalizedPositionsLayerOffset = Stream.of(fieldNormalizedPositionsFieldOffset)
                .parallel()
                .map(p -> p.offset(Direction.DOWN, extraYOffset))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        for(BlockPos normalizedFieldPosition : fieldNormalizedPositionsLayerOffset) {

            // normalizedFieldPosition is the normalized position in the ROTATED layout
            boolean required = l.isPositionRequired(normalizedFieldPosition);
            if(!required) {
                // is block set? - if so, exit as a failure
            }

            String requiredCompKey = l.getRequiredComponentKeyForPosition(normalizedFieldPosition);
            if(requiredCompKey == null) {
                CompactCrafting.LOGGER.error("Relative position marked as required but the recipe layer did not have a lookup.");
                return false;
            }

//            Optional<String> realComponentInPosition = this.getRecipeComponentKey(state);
//
//
//
//            // No lookup defined in the recipe for the state in the position
//            if(!realComponentInPosition.isPresent())
//                return false;
//
//            // Does component match in position?
//            if(!realComponentInPosition.get().equals(requiredCompKey))
//                return false;
        }

        return true;
    }

    private Optional<IRecipeLayer> getRecipeLayerFromPositions(AxisAlignedBB fieldFilledBounds, BlockPos[] filledPositions) {
        if(filledPositions.length == 0)
            return Optional.empty();

        int filledYLevel = filledPositions[0].getY();
        int minFilledLevel = (int) Math.floor(fieldFilledBounds.minY);
        int yLevelRelative = filledYLevel - minFilledLevel;

        return this.getLayer(yLevelRelative);
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if(y < 0 || y > this.layers.length - 1)
            return Optional.empty();

        return Optional.of(this.layers[y]);
    }
}
