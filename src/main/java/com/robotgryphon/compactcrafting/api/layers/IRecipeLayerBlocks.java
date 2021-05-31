package com.robotgryphon.compactcrafting.api.layers;

import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface IRecipeLayerBlocks {

    BlockState getRelativeState(BlockPos relativePos);

    /**
     * Rotates a set of blocks in-place, and returns the new rotated instance. (Immutable)
     * @param rotation
     * @return
     */
    IRecipeLayerBlocks rotate(Rotation rotation);

    int getNumberFilled();

    /**
     * Gets the number of unique component keys in this set of blocks.
     * @return
     */
    int getNumberUniqueComponents();

    Map<String, Integer> getComponentTotals();
}
