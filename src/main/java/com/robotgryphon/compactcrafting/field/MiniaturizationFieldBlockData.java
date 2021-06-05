package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class MiniaturizationFieldBlockData {

    private AxisAlignedBB filledBounds;

    private BlockPos[] filledPositions;

    private MiniaturizationFieldBlockData() {
    }

    public static MiniaturizationFieldBlockData getFromField(IWorldReader world, AxisAlignedBB field) {
        MiniaturizationFieldBlockData fb = new MiniaturizationFieldBlockData();

        BlockPos[] nonAirPositions = BlockPos.betweenClosedStream(field)
                .filter(p -> !world.isEmptyBlock(p))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        fb.filledPositions = nonAirPositions;

        fb.filledBounds = BlockSpaceUtil.getBoundsForBlocks(nonAirPositions);

        return fb;
    }

    public AxisAlignedBB getFilledBounds() {
        return this.filledBounds;
    }

    public int getNumberFilledBlocks() {
        return filledPositions.length;
    }

}
