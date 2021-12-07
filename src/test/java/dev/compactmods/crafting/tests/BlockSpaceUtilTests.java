package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockSpaceUtilTests {

    @Test
    void doesNormalizeSingleBlockPos() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        var field = new AABB(min, max);

        BlockPos actual = BlockSpaceUtil.normalizeLayerPosition(field, min);

        Assertions.assertEquals(BlockPos.ZERO, actual);
    }

    @Test
    void doesNormalizeMultipleBlockPos() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = new AABB(min, max);

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
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = new AABB(min, max);

        BlockPos original = new BlockPos(100, 0, 100);

        BlockPos norm = BlockSpaceUtil.normalizeLayerPosition(field, original);

        BlockPos denorm = BlockSpaceUtil.denormalizeLayerPosition(field, norm);

        Assertions.assertEquals(original, denorm);
    }
}
