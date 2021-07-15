package com.robotgryphon.compactcrafting.tests.util;

import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockSpaceUtilTests {

    @Test
    void doesNormalizeSingleBlockPos() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = FieldProjectionSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AxisAlignedBB field = new AxisAlignedBB(min, max);

        BlockPos actual = BlockSpaceUtil.normalizeLayerPosition(field, min);

        Assertions.assertEquals(BlockPos.ZERO, actual);
    }

    @Test
    void doesNormalizeMultipleBlockPos() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = FieldProjectionSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AxisAlignedBB field = new AxisAlignedBB(min, max);

        BlockPos[] actual = BlockSpaceUtil.normalizeLayerPositions(field, new BlockPos[]{
                new BlockPos(100, 0, 100),
                new BlockPos(102, 0, 102),
                new BlockPos(102, 2, 102)
        });

        Assertions.assertArrayEquals(new BlockPos[]{
                new BlockPos(0, 0, 0),
                new BlockPos(2, 0, 2),
                new BlockPos(2, 2, 2)
        }, actual);
    }

    @Test
    void doesDenormalizePosition() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = FieldProjectionSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AxisAlignedBB field = new AxisAlignedBB(min, max);

        BlockPos original = new BlockPos(100, 0, 100);

        BlockPos norm = BlockSpaceUtil.normalizeLayerPosition(field, original);

        BlockPos denorm = BlockSpaceUtil.denormalizeLayerPosition(field, norm);

        Assertions.assertEquals(original, denorm);
    }
}
