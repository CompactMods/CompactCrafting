package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IWorldReader;

import java.util.Collections;
import java.util.Map;

public class SingleComponentRecipeLayer implements IRecipeLayer {
    public SingleComponentRecipeLayer(Item item, AxisAlignedBB size) {
    }

    @Override
    public boolean matchesFieldLayer(IWorldReader world, AxisAlignedBB fieldLayer) {
        return true;
    }

    @Override
    public Map<String, BlockState> getComponents() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return Collections.emptyMap();
    }
}
