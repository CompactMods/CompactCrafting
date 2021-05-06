package com.robotgryphon.compactcrafting.recipes.layers;

import com.robotgryphon.compactcrafting.recipes.components.RecipeComponent;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IRecipeLayer {
    Map<String, Integer> getComponentTotals(Map<String, ? extends RecipeComponent> componentMap);

    /**
     * Gets a component key for the given (normalized) position.
     *
     * @param pos
     * @return
     */
    Optional<String> getRequiredComponentKeyForPosition(BlockPos pos);

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    Collection<BlockPos> getPositionsForComponent(String component);

    /**
     * Gets a set of non-air positions that are required for the layer to match.
     * This is expected to trim the air positions off the edges and return the positions with NW
     * in the 0, 0 position.
     *
     * @return
     */
    Collection<BlockPos> getFilledPositions();

    boolean isPositionFilled(BlockPos pos);

    int getNumberFilledPositions(Map<String, ? extends RecipeComponent> componentMap);

    RecipeLayerType<?> getType();
}
