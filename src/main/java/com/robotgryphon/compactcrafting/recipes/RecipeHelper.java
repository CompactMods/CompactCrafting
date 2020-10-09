package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Stream;

public abstract class RecipeHelper {

    public static BlockPos[] normalizeFieldPositions(MiniaturizationFieldBlockData fieldBlocks) {
        BlockPos[] filledBlocks = fieldBlocks.getFilledBlocks();
        AxisAlignedBB filledBounds = fieldBlocks.getFilledBounds();

        return Stream.of(filledBlocks)
                .parallel()
                .map(p -> BlockSpaceUtil.normalizeLayerPosition(filledBounds, p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }
}
