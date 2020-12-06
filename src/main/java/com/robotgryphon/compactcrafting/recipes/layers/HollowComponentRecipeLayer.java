package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HollowComponentRecipeLayer implements IRecipeLayer, IDynamicRecipeLayer {

    private String componentKey;
    private AxisAlignedBB recipeDimensions;
    private Collection<BlockPos> filledPositions;

    public HollowComponentRecipeLayer(String component) {
        this.componentKey = component;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return Collections.singletonMap(componentKey, getNumberFilledPositions());
    }

    @Override
    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        return componentKey;
    }

    @Override
    public Collection<BlockPos> getNonAirPositions() {
        return this.filledPositions;
    }

    @Override
    public boolean isPositionRequired(BlockPos pos) {
        return true;
    }

    @Override
    public int getNumberFilledPositions() {
        return filledPositions.size();
    }

    public void setComponent(String component) {
        this.componentKey = component;
    }

    @Override
    public void setRecipeDimensions(AxisAlignedBB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    @Override
    public void recalculateRequirements() {
        this.filledPositions = getWallPositions();
    }

    public Collection<BlockPos> getWallPositions() {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, recipeDimensions.getXSize() - 1, 0, recipeDimensions.getZSize() - 1);
        AxisAlignedBB insideBounds = layerBounds.offset(1, 0, 1).contract(2, 0, 2);

        Set<BlockPos> positions = BlockPos.getAllInBox(layerBounds)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toSet());

        Set<BlockPos> inside = BlockPos.getAllInBox(insideBounds)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toSet());

        positions.removeAll(inside);
        return positions;
    }
}
