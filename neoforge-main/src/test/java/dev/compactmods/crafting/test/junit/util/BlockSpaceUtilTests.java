package dev.compactmods.crafting.test.junit.util;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(EphemeralTestServerProvider.class)
public class BlockSpaceUtilTests {

    @Test
    public void can_splice_single_layer() {
        AABB fullBounds = new AABB(0, 0, 0, 10, 10, 10);
        AABB slice = new AABB(0, 0, 0, 10, 1, 10);

        final AABB actual = BlockSpaceUtil.getLayerBounds(fullBounds, 0);

        if (!Objects.equals(slice, actual))
            Assertions.fail("Slice did not equal actual returned value.");
    }

    @Test
    public void no_blocks_equals_empty_axis_bounds() {
        final Set<BlockPos> positions = Collections.emptySet();

        final AABB filledBounds = BlockSpaceUtil.getBoundsForBlocks(positions);

        Assertions.assertEquals(new AABB(0, 0, 0, 0, 0, 0), filledBounds);
    }

    @Test
    public void calculates_center_bounds_odd() {
        AABB fullBounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        if (centerBounds.getXsize() != 1) Assertions.fail("X dimensions were not correct.");
        if (centerBounds.getYsize() != 1) Assertions.fail("Y dimensions were not correct.");
        if (centerBounds.getZsize() != 1) Assertions.fail("Z dimensions were not correct.");

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));
    }

    @Test
    public void calculates_center_bounds_even() {
        AABB fullBounds = new AABB(0, 0, 0, 6, 1, 6);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        if (centerBounds.getXsize() != 2) Assertions.fail("X dimensions were not correct.");
        if (centerBounds.getYsize() != 1) Assertions.fail("Y dimensions were not correct.");
        if (centerBounds.getZsize() != 2) Assertions.fail("Z dimensions were not correct");

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        if (positions.isEmpty())
            Assertions.fail("Expected matched positions.");

        if (positions.size() != 4)
            Assertions.fail("Expected 4 matched positions; got " + positions.size());

        Assertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));
        Assertions.assertTrue(positions.contains(new BlockPos(3, 0, 3)));
    }

    @Test
    public void bounds_fits_inside() {
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
    public void can_get_layer_block_positions() {
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
    public void single_block_rotates_correctly() {
        BlockPos[] singleBlock = new BlockPos[]{
                new BlockPos(1, 0, 0)
        };

        Map<BlockPos, BlockPos> newLocations = BlockSpaceUtil.rotatePositionsInPlace(singleBlock);

        BlockPos rotatedPos = newLocations.get(singleBlock[0]);

        Assertions.assertEquals(new BlockPos(1, 0, 0), rotatedPos);
    }

    @Test
    public void complex_shape_rotates_in_place_correctly() {
        /*
            Magnifying glass shape.

             ---XX-
             --X--X
             --X--X
             --XXX-
             -X----
             X-----
         */
        BlockPos[] complexPattern = new BlockPos[]{
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
        BlockPos[] relativeWestPositions = new BlockPos[]{
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
    public void complex_shape_rotates_180_correctly() {
        /*
            Magnifying glass shape.

             ---XX-
             --X--X
             --X--X
             --XXX-
             -X----
             X-----
         */
        BlockPos[] complexPreTranslate = new BlockPos[]{
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
        BlockPos[] relativeWestPositions = new BlockPos[]{
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
    public void does_normalize_single_block_pos() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        var field = AABB.encapsulatingFullBlocks(min, max);

        BlockPos actual = BlockSpaceUtil.normalizeLayerPosition(field, min);

        Assertions.assertEquals(BlockPos.ZERO, actual);
    }

    @Test
    public void denormalizes_multiple_positions() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = AABB.encapsulatingFullBlocks(min, max);

        final var expected = new BlockPos[]{
                new BlockPos(0, 0, 0),
                new BlockPos(2, 0, 2),
                new BlockPos(2, 2, 2)
        };

        BlockPos[] actual = BlockSpaceUtil.normalizeLayerPositions(field, new BlockPos[]{
                new BlockPos(100, 0, 100),
                new BlockPos(102, 0, 102),
                new BlockPos(102, 2, 102)
        });

        if (actual.length != 3)
            Assertions.fail("Expected 3 positions after normalization; got " + actual.length);

        for (int i = 0; i < 3; i++) {
            BlockPos exp = expected[i];
            BlockPos act = actual[i];
            if (!exp.equals(act))
                Assertions.fail("Expected positions to match [" + i + "]: " + exp + " vs. " + act);
        }
    }

    @Test
    public void denormalizes_single_position() {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = AABB.encapsulatingFullBlocks(min, max);

        BlockPos original = new BlockPos(100, 0, 100);

        BlockPos norm = BlockSpaceUtil.normalizeLayerPosition(field, original);

        BlockPos denorm = BlockSpaceUtil.denormalizeLayerPosition(field, norm);

        Assertions.assertEquals(original, denorm);
    }
}
