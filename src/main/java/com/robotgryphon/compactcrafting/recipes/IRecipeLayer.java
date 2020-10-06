package com.robotgryphon.compactcrafting.recipes;

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

    boolean isPositionRequired(BlockPos pos);

    int getNumberFilledPositions();
}
