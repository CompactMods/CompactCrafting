package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayer;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SingleComponentRecipeLayer implements IRecipeLayer, IRigidRecipeLayer {

    private String componentKey;
    private AxisAlignedBB dimensions;

    /**
     * Relative positions that are filled by the component.
     * Example:
     *
     *   X ----->
     * Z   X-X
     * |   -X-
     * |  -X-X
     *
     * = [0, 0], [2, 0], [1, 1], [0, 2], [2, 2]
     */
    private Collection<BlockPos> filledPositions;

    protected SingleComponentRecipeLayer(String key) {
        this.componentKey = key;
    }

    public SingleComponentRecipeLayer(String key, Collection<BlockPos> filledPositions) {
        this.componentKey = key;
        this.filledPositions = filledPositions;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(filledPositions);
    }

    @Override
    public AxisAlignedBB getDimensions() {
        return dimensions;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        double volume = dimensions.getXSize() * dimensions.getYSize() * dimensions.getZSize();
        return Collections.singletonMap(componentKey, (int) Math.ceil(volume));
    }

    @Override
    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        return filledPositions.contains(pos) ? componentKey : null;
    }

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    @Override
    public Collection<BlockPos> getPositionsForComponent(String component) {
        if(component == this.componentKey)
            return filledPositions;

        return Collections.emptySet();
    }

    @Override
    public Collection<BlockPos> getNonAirPositions() {
        return filledPositions;
    }

    @Override
    public boolean isPositionRequired(BlockPos pos) {
        return filledPositions.contains(pos);
    }

    @Override
    public int getNumberFilledPositions() {
        return filledPositions.size();
    }

    @Override
    public <T extends IRecipeLayer> RecipeLayerSerializer<T> getSerializer(T layer) {
        return null;
    }
}
