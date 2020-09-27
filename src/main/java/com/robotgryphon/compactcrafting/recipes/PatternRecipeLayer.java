package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.Map;

public class PatternRecipeLayer implements IRecipeLayer {
    @Override
    public boolean matchesFieldLayer(IWorldReader world, AxisAlignedBB fieldLayer) {
        return false;
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public Vector3i getDimensions() {
        return null;
    }

    @Override
    public Vector3i getRelativeOffset() {
        return null;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return null;
    }
}
