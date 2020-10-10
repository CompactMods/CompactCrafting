package com.robotgryphon.compactcrafting.recipes.layers;

import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MixedComponentRecipeLayer implements IRecipeLayer, IFixedLayerDimensions {
    private AxisAlignedBB dimensions;
    private Map<BlockPos, String> componentLookup;
    private Map<String, Integer> totalCache;

    public MixedComponentRecipeLayer() {
        this.dimensions = AxisAlignedBB.withSizeAtOrigin(0, 0, 0);
        this.componentLookup = new HashMap<>();
    }

    public void add(String component, BlockPos location) {
        componentLookup.putIfAbsent(location, component);
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.keySet());
    }

    public void addMultiple(String component, Collection<BlockPos> locations) {
        boolean recalc = false;
        for(BlockPos loc : locations) {
            if(!componentLookup.containsKey(loc)) {
                componentLookup.put(loc, component);
                recalc = true;
            }
        }

        if(recalc)
            this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.keySet());
    }

    @Override
    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    @Override
    public Map<String, Integer> getComponentTotals(AxisAlignedBB recipeDims) {
        if(this.totalCache != null)
            return this.totalCache;

        Map<String, Integer> totals = new HashMap<>();
        componentLookup.forEach((pos, comp) -> {
            int prev = 0;
            if(!totals.containsKey(comp))
                totals.put(comp, 0);
            else
                prev = totals.get(comp);

            totals.replace(comp, prev + 1);
        });

        this.totalCache = totals;

        return this.totalCache;
    }

    @Override
    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        if(componentLookup.containsKey(pos))
            return componentLookup.get(pos);

        return null;
    }

    @Override
    public Collection<BlockPos> getNonAirPositions(AxisAlignedBB recipeDims) {
        return componentLookup.keySet();
    }

    @Override
    public boolean isPositionRequired(BlockPos pos) {
        return componentLookup.containsKey(pos);
    }

    @Override
    public int getNumberFilledPositions(AxisAlignedBB recipeDims) {
        return getComponentTotals(recipeDims)
                .values()
                .stream()
                .reduce(0, Integer::sum);
    }
}
