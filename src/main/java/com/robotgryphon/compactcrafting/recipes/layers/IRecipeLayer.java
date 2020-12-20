package com.robotgryphon.compactcrafting.recipes.layers;

import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.FilledComponentRecipeLayer;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Map;

public interface IRecipeLayer {

    Map<String, Integer> getComponentTotals();

    /**
     * Gets a component key for the given (normalized) position.
     *
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

    <T extends IRecipeLayer> RecipeLayerSerializer<FilledComponentRecipeLayer> getSerializer(T layer);
}
