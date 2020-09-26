package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IWorldReader;

import java.util.Map;

public class PatternRecipeLayer implements IRecipeLayer {
    @Override
    public boolean matchesFieldLayer(IWorldReader world, AxisAlignedBB fieldLayer) {
        return false;
    }

    @Override
    public Map<String, BlockState> getComponents() {
        return null;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return null;
    }
}
