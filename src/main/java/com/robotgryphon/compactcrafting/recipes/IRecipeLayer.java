package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.Map;

public interface IRecipeLayer {

    /**
     * Determines if this layer matches a field's layer, given the blocks in the field
     * and the definition of the recipe layer. This is expected to rotate and check all four
     * cardinal directions.
     *
     * @param world
     * @param fieldLayer
     * @return
     */
    boolean matchesFieldLayer(IWorldReader world, AxisAlignedBB fieldLayer);

    int getVolume();

    Vector3i getDimensions();

    /**
     * Relative offset from center; if BlockPos.ZERO, center of this is assumed to be the center
     * of the layer.
     *
     * @return
     */
    Vector3i getRelativeOffset();

    Map<String, Integer> getComponentTotals();
}
