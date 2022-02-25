package dev.compactmods.crafting.tests.util;

import java.util.*;
import java.util.stream.Collectors;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
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

    @Test
    void SingleBlockRotatesCorrectly() {
        BlockPos[] singleBlock = new BlockPos[] {
                new BlockPos(1, 0, 0)
        };

        Map<BlockPos, BlockPos> newLocations = BlockSpaceUtil.rotatePositionsInPlace(singleBlock);

        BlockPos rotatedPos = newLocations.get(singleBlock[0]);

        Assertions.assertEquals(new BlockPos(1, 0, 0), rotatedPos);
    }

    @Test
    void ComplexShapeRotatesInPlaceCorrectly() {
        /*
            Magnifying glass shape.

             ---XX-
             --X--X
             --X--X
             --XXX-
             -X----
             X-----
         */
        BlockPos[] complexPattern = new BlockPos[] {
                // Glass
                new BlockPos(3, 0, 0),
                new BlockPos(4, 0, 0),
                new BlockPos(2, 0, 1),
                new BlockPos(5, 0, 1),
                new BlockPos(2, 0, 2),
                new BlockPos(5, 0, 2),
                new BlockPos(3, 0, 3),
                new BlockPos(4, 0, 3),

                // Tail
                new BlockPos(2, 0, 3),
                new BlockPos(1, 0, 4),
                new BlockPos(0, 0, 5)
        };

        /*
            Magnifying glass shape. (West/90-degree rotation)

                X-----
                -X----
                --XXX-
                -X---X
                -X---X
                --xxx-
         */
        BlockPos[] relativeWestPositions = new BlockPos[] {
                // Glass
                new BlockPos(3, 0, 2),
                new BlockPos(4, 0, 2),
                new BlockPos(2, 0, 3),
                new BlockPos(5, 0, 3),
                new BlockPos(2, 0, 4),
                new BlockPos(5, 0, 4),
                new BlockPos(3, 0, 5),
                new BlockPos(4, 0, 5),

                // Tail
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 1),
                new BlockPos(2, 0, 2)
        };

        Map<BlockPos, BlockPos> rotatedPattern = BlockSpaceUtil.rotatePositionsInPlace(complexPattern, Rotation.CLOCKWISE_90);

        List<BlockPos> expected = Arrays.asList(relativeWestPositions);
        List<BlockPos> actual = Arrays.asList(rotatedPattern.values().toArray(new BlockPos[0]));

        Assertions.assertTrue(actual.containsAll(expected));
    }

    @Test
    void ComplexShapeRotates180Correctly () {
        /*
            Magnifying glass shape.

             ---XX-
             --X--X
             --X--X
             --XXX-
             -X----
             X-----
         */
        BlockPos[] complexPreTranslate = new BlockPos[] {
                // Glass
                new BlockPos(3, 0, 0),
                new BlockPos(4, 0, 0),
                new BlockPos(2, 0, 1),
                new BlockPos(5, 0, 1),
                new BlockPos(2, 0, 2),
                new BlockPos(5, 0, 2),
                new BlockPos(3, 0, 3),
                new BlockPos(4, 0, 3),

                // Tail
                new BlockPos(2, 0, 3),
                new BlockPos(1, 0, 4),
                new BlockPos(0, 0, 5)
        };

        /*
            Magnifying glass shape. (West/90-degree rotation)

                X-----
                -X----
                --XXX-
                -X---X
                -X---X
                --xxx-
         */
        BlockPos[] relativeWestPositions = new BlockPos[] {
                // Glass
                new BlockPos(1, 0, 2),
                new BlockPos(2, 0, 2),
                new BlockPos(0, 0, 3),
                new BlockPos(3, 0, 3),
                new BlockPos(0, 0, 4),
                new BlockPos(3, 0, 4),
                new BlockPos(1, 0, 5),
                new BlockPos(2, 0, 5),

                // Tail
                new BlockPos(3, 0, 2),
                new BlockPos(4, 0, 1),
                new BlockPos(5, 0, 0)
        };

        Map<BlockPos, BlockPos> rotatedPattern = BlockSpaceUtil.rotatePositionsInPlace(complexPreTranslate, Rotation.CLOCKWISE_180);

        List<BlockPos> expected = Arrays.asList(relativeWestPositions);
        List<BlockPos> actual = Arrays.asList(rotatedPattern.values().toArray(new BlockPos[0]));

        Assertions.assertTrue(actual.containsAll(expected));
    }

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
