package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class RecipeLayer {
    public abstract Map<String, Integer> getComponentTotals();

    /**
     * Gets a component key for the given (normalized) position.
     *
     * @param pos
     * @return
     */
    public abstract Optional<String> getRequiredComponentKeyForPosition(BlockPos pos);

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    public abstract Collection<BlockPos> getPositionsForComponent(String component);

    /**
     * Gets a set of non-air positions that are required for the layer to match.
     * This is expected to trim the air positions off the edges and return the positions with NW
     * in the 0, 0 position.
     *
     * @return
     */
    public abstract Collection<BlockPos> getFilledPositions();

    public abstract boolean isPositionFilled(BlockPos pos);

    public abstract int getNumberFilledPositions();

    public abstract RecipeLayerType<?> getType();
}
