package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Collection;
import java.util.Map;

public interface IRecipeLayer {

    /**
     * Specifies if the current layer needs to add padding spaces (air)
     * around a recipe template.
     *
     * @param world
     * @param recipe
     * @return
     */
    boolean hasPadding(IWorldReader world, MiniaturizationRecipe recipe);

    /**
     * Determines if this layer matches a field's layer, given the blocks in the field
     * and the definition of the recipe layer. This is expected to rotate and check all four
     * cardinal directions.
     *
     * @param world
     * @param recipe
     * @param fieldSize
     * @param fieldLayer
     * @return
     */
    boolean matchesFieldLayer(IWorldReader world, MiniaturizationRecipe recipe, FieldProjectionSize fieldSize, AxisAlignedBB fieldLayer);

    /**
     * Gets the trimmed dimensions of the given recipe layer.
     *
     * @return
     */
    AxisAlignedBB getDimensions();

    Map<String, Integer> getComponentTotals();

    /**
     * Gets a component key for the given (normalized) position.
     * @param pos
     * @return
     */
    String getRequiredComponentKeyForPosition(BlockPos pos);

    /**
     * Gets a set of non-air positions that are required for the layer to match.
     * This is expected to trim the air positions off the edges and return the positions with NW
     * in the 0, 0 position.
     *
     * @return
     */
    Collection<BlockPos> getNonAirPositions();
}
