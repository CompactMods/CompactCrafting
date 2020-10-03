package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.FieldHelper;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Stream;

public class SingleComponentRecipeLayer implements IRecipeLayer {

    private String componentKey;
    private AxisAlignedBB dimensions;

    /**
     * Relative positions that are filled by the component.
     * Example:
     *
     *   X ----->
     * Z   X-X
     * |   -X-
     * |  -X-X
     *
     * = [0, 0], [2, 0], [1, 1], [0, 2], [2, 2]
     */
    private Set<BlockPos> filledPositions;

    public SingleComponentRecipeLayer(String key, Set<BlockPos> filledPositions) {
        this.componentKey = key;
        this.filledPositions = filledPositions;
        this.dimensions = AxisAlignedBB.withSizeAtOrigin(0, 0, 0);
    }

    @Override
    public boolean hasPadding(IWorldReader world, MiniaturizationRecipe recipe) {
        return false;
    }

    @Override
    public boolean matchesFieldLayer(IWorldReader world, MiniaturizationRecipe recipe, FieldProjectionSize fieldSize, AxisAlignedBB fieldLayer) {
        Optional<BlockState> component = recipe.getRecipeComponent(componentKey);
        // We can't find a component definition in the recipe, so something very wrong happened
        if(!component.isPresent()) {
            CompactCrafting.LOGGER.warn(
                String.format("Attempted to find component with key '%s' but no component was found.", componentKey)
            );

            return false;
        }

        // Dimensions check - make sure the field layer is large enough for the recipe layer
        if(this.dimensions.getXSize() > fieldLayer.getXSize() || this.dimensions.getZSize() > fieldLayer.getZSize())
            return false;

        // Non-empty field positions
        BlockPos[] fieldPositions = BlockPos.getAllInBox(fieldLayer)
                .filter(p -> !world.isAirBlock(p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        // Get all the distinct block types in the current layer
        Stream<BlockState> distinctStates = Arrays.stream(fieldPositions)
                .map(world::getBlockState)
                .distinct();

        // Invalid block found - early exit
        if(distinctStates.anyMatch(state -> state != component.get()))
            return false;

        BlockPos[] normalizedFilledPositions = RecipeHelper.normalizeLayerPositions(this.dimensions, fieldPositions);

        // Create a minimum-filled bounds of blocks in the field
        AxisAlignedBB trimmedBounds = RecipeHelper.getBoundsForBlocks(Arrays.asList(normalizedFilledPositions));

        // Whitespace trim done - no padding needed, min and max bounds are already correct

        // Check recipe template against padded world layout
        Map<BlockPos, String> templateMap = FieldHelper.remapLayerToRecipe(world, recipe, fieldSize, fieldLayer);

        return RecipeHelper.layerMatchesTemplate(world, recipe, this, fieldLayer, normalizedFilledPositions);
    }

    @Override
    public AxisAlignedBB getDimensions() {
        return dimensions;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        double volume = dimensions.getXSize() * dimensions.getYSize() * dimensions.getZSize();
        return Collections.singletonMap(componentKey, (int) Math.ceil(volume));
    }

    @Override
    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        return filledPositions.contains(pos) ? componentKey : null;
    }

    @Override
    public Set<BlockPos> getNonAirPositions() {
        return filledPositions;
    }
}
