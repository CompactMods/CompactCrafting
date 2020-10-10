package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class FilledComponentRecipeLayer implements IRecipeLayer {

    private String componentKey;

    public FilledComponentRecipeLayer(String component) {
        this.componentKey = component;
    }

    @Override
    public Map<String, Integer> getComponentTotals(AxisAlignedBB recipeDims) {
        return Collections.singletonMap(componentKey, getNumberFilledPositions(recipeDims));
    }

    @Override
    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        return componentKey;
    }

    @Override
    public Collection<BlockPos> getNonAirPositions(AxisAlignedBB recipeDims) {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, recipeDims.getXSize(), 1, recipeDims.getZSize());
        return BlockPos.getAllInBox(layerBounds)
                .parallel()
                .map(BlockPos::toImmutable)
                .collect(Collectors.toSet());

    }

    @Override
    public boolean isPositionRequired(BlockPos pos) {
        return true;
    }

    @Override
    public int getNumberFilledPositions(AxisAlignedBB recipeDims) {
        return (int) Math.ceil(recipeDims.getXSize() * recipeDims.getYSize());
    }

    public void setComponent(String component) {
        this.componentKey = component;
    }
}
