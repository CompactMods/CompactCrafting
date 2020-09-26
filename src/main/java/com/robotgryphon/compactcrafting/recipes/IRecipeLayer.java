package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
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

    Map<String, BlockState> getComponents();

    Map<String, Integer> getComponentTotals();
}
