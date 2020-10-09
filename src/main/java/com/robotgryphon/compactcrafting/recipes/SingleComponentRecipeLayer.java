package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SingleComponentRecipeLayer implements IRecipeLayer {

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

    public SingleComponentRecipeLayer(String key, Collection<BlockPos> filledPositions) {
        this.componentKey = key;
        this.filledPositions = filledPositions;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(filledPositions);
    }

    @Override
    public boolean hasPadding(IWorldReader world, MiniaturizationRecipe recipe) {
        return false;
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
}
