package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.stream.Stream;

public class MiniaturizationFieldBlockData {

    private AxisAlignedBB fieldBounds;
    private AxisAlignedBB filledBounds;

    private BlockPos[] filledPositions;
    private BlockPos[] relativeFilledPositions;

    private MiniaturizationFieldBlockData(AxisAlignedBB fieldBounds) {
        this.fieldBounds = fieldBounds;
    }

    public static MiniaturizationFieldBlockData getFromField(IWorldReader world, AxisAlignedBB field) {
        MiniaturizationFieldBlockData fb = new MiniaturizationFieldBlockData(field);

        BlockPos[] nonAirPositions = BlockPos.getAllInBox(field)
                .filter(p -> !world.isAirBlock(p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        fb.filledPositions = nonAirPositions;

        fb.filledBounds = BlockSpaceUtil.getBoundsForBlocks(nonAirPositions);

        fb.relativeFilledPositions = RecipeHelper.normalizeFieldPositions(fb);

        return fb;
    }

    public AxisAlignedBB getFilledBounds() {
        return this.filledBounds;
    }

    public int getNumberFilledBlocks() {
        return filledPositions.length;
    }

    public BlockPos[] getFilledBlocks() {
        return this.filledPositions;
    }

    public Stream<BlockPos> getRelativeFilledBlocks() {
        return Stream.of(relativeFilledPositions);
    }
}
