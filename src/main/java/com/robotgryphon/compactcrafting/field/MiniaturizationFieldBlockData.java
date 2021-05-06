package com.robotgryphon.compactcrafting.field;

import com.google.common.collect.ImmutableList;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class MiniaturizationFieldBlockData {
    private final AxisAlignedBB fieldBounds;
    private final AxisAlignedBB filledBounds;
    private final ImmutableList<BlockPos> filledPositions;
    private final ImmutableList<BlockPos> relativeFilledPositions;

    private MiniaturizationFieldBlockData(AxisAlignedBB fieldBounds, AxisAlignedBB filledBounds, ImmutableList<BlockPos> filledPositions, ImmutableList<BlockPos> relativeFilledPositions) {
        this.fieldBounds = fieldBounds;
        this.filledBounds = filledBounds;
        this.filledPositions = filledPositions;
        this.relativeFilledPositions = relativeFilledPositions;
    }

    public static MiniaturizationFieldBlockData getFromField(IWorldReader world, AxisAlignedBB field) {
        ImmutableList<BlockPos> filledPositions = BlockPos.betweenClosedStream(field)
                .filter(p -> !world.isEmptyBlock(p))
                .map(BlockPos::immutable)
                .collect(ImmutableList.toImmutableList());

        AxisAlignedBB filledBounds = BlockSpaceUtil.getBoundsForBlocks(filledPositions);
        ImmutableList<BlockPos> relativeFilledPositions = RecipeHelper.normalizeFieldPositions(filledPositions, filledBounds);

        return new MiniaturizationFieldBlockData(field, filledBounds, filledPositions, relativeFilledPositions);
    }

    public AxisAlignedBB getFieldBounds() {
        return this.fieldBounds;
    }

    public AxisAlignedBB getFilledBounds() {
        return this.filledBounds;
    }

    public AxisAlignedBB getRelativeFilledBounds() {
        return this.filledBounds.move(-this.fieldBounds.minX, -this.fieldBounds.minY, -this.fieldBounds.minZ);
    }

    public ImmutableList<BlockPos> getFilledBlocks() {
        return this.filledPositions;
    }

    public ImmutableList<BlockPos> getRelativeFilledBlocks() {
        return this.relativeFilledPositions;
    }
}
