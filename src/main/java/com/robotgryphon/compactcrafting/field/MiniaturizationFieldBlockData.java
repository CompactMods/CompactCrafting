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

        // we contract here due to betweenClosedStream returning excess block positions
        BlockPos[] nonAirPositions = BlockPos.betweenClosedStream(field.contract(1, 1, 1))
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
