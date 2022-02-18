package dev.compactmods.crafting.tests.recipes.util;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockSpaceUtilTests {

    @Test
    void CanSpliceSingleLayer() {
        AABB fullBounds = new AABB(0, 0, 0, 10, 10, 10);
        AABB slice = new AABB(0, 0, 0, 10, 1, 10);

        final AABB actual = BlockSpaceUtil.getLayerBounds(fullBounds, 0);

        Assertions.assertEquals(slice, actual, "Slice did not equal actual returned value.");
    }

    @Test
    void NoBlocksEqualsEmptyAxisBounds() {
        final Set<BlockPos> positions = Collections.emptySet();

        final AABB filledBounds = BlockSpaceUtil.getBoundsForBlocks(positions);

        Assertions.assertEquals(new AABB(0, 0, 0, 0, 0, 0), filledBounds);
    }

    @Test
    void CanCalculateCenterBoundsOdd() {
        AABB fullBounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        Assertions.assertEquals(1, centerBounds.getXsize());
        Assertions.assertEquals(1, centerBounds.getYsize());
        Assertions.assertEquals(1, centerBounds.getZsize());

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));
    }

    @Test
    void CanCalculateCenterBoundsEven() {
        AABB fullBounds = new AABB(0, 0, 0, 6, 1, 6);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        Assertions.assertEquals(2, centerBounds.getXsize());
        Assertions.assertEquals(1, centerBounds.getYsize());
        Assertions.assertEquals(2, centerBounds.getZsize());

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertEquals(4, positions.size());
        Assertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));
        Assertions.assertTrue(positions.contains(new BlockPos(3, 0, 3)));
    }

    @Test
    void BSBoundsFitsInside() {
        AABB outer = new AABB(0, 0, 0, 10, 10, 10);
        AABB inner = new AABB(1, 1, 1, 3, 3, 3);

        Assertions.assertTrue(BlockSpaceUtil.boundsFitsInside(inner, outer));

        AABB unit = new AABB(0, 0, 0, 1, 1, 1);
        AABB unitX = unit.expandTowards(1, 0, 0);
        AABB unitY = unit.expandTowards(0, 1, 0);
        AABB unitZ = unit.expandTowards(0, 0, 1);

        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitX, unit), "Unit should not fit in X dimension but does.");
        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitY, unit), "Unit should not fit in Y dimension but does.");
        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitZ, unit), "Unit should not fit in Z dimension but does.");
    }

    @Test
    void CanGetLayerBlockPositions() {
        AABB layer = new AABB(0, 0, 0, 5, 1, 5);

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(layer)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertNotNull(positions);
        Assertions.assertFalse(positions.isEmpty());

        Assertions.assertEquals(25, positions.size());

        Assertions.assertTrue(positions.contains(new BlockPos(0, 0, 0)));
        Assertions.assertTrue(positions.contains(new BlockPos(4, 0, 4)));

        Assertions.assertFalse(positions.contains(new BlockPos(0, 1, 0)));
    }
}
