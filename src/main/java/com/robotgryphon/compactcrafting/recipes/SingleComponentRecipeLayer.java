package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.Collections;
import java.util.Map;

public class SingleComponentRecipeLayer implements IRecipeLayer {

    private String componentKey;
    private Vector3i dimensions;

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
    private BlockPos[] layerGrid;

    public SingleComponentRecipeLayer(String key, BlockPos[] layerGrid) {
        this.componentKey = key;

        recalculateDimensions();
    }

    @Override
    public boolean matchesFieldLayer(IWorldReader world, AxisAlignedBB fieldLayer) {
        return true;
    }

    private Vector3i recalculateDimensions() {
        this.dimensions = new Vector3i(1, 1, 1);
        return this.dimensions;
    }

    @Override
    public int getVolume() {
        Vector3i dim = getDimensions();
        return dim.getX() * dim.getY() * dim.getZ();
    }

    @Override
    public Vector3i getDimensions() {
        return dimensions;
    }

    @Override
    public Vector3i getRelativeOffset() {
        return Vector3i.NULL_VECTOR;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return Collections.singletonMap(componentKey, getVolume());
    }
}
