package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.Collections;
import java.util.Map;

public class PatternRecipeLayer implements IRecipeLayer {
    @Override
    public boolean matchesFieldLayer(IWorldReader world, MiniaturizationRecipe recipe, AxisAlignedBB fieldLayer) {
        return false;
    }

    @Override
    public Vector3i getDimensions() {
        return Vector3i.NULL_VECTOR;
    }

    @Override
    public Vector3i getRelativeOffset() {
        return Vector3i.NULL_VECTOR;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return Collections.emptyMap();
    }
}
