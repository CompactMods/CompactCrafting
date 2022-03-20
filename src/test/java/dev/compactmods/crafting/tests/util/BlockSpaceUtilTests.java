package dev.compactmods.crafting.tests.util;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class BlockSpaceUtilTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanSpliceSingleLayer(final GameTestHelper test) {
        AABB fullBounds = new AABB(0, 0, 0, 10, 10, 10);
        AABB slice = new AABB(0, 0, 0, 10, 1, 10);

        final AABB actual = BlockSpaceUtil.getLayerBounds(fullBounds, 0);

        if (!Objects.equals(slice, actual))
            test.fail("Slice did not equal actual returned value.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void no_blocks_equals_empty_axis_bounds(final GameTestHelper test) {
        final Set<BlockPos> positions = Collections.emptySet();

        final AABB filledBounds = BlockSpaceUtil.getBoundsForBlocks(positions);

        GameTestAssertions.assertEquals(new AABB(0, 0, 0, 0, 0, 0), filledBounds);
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void calculates_center_bounds_odd(final GameTestHelper test) {
        AABB fullBounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        if(centerBounds.getXsize() != 1) test.fail("X dimensions were not correct.");
        if(centerBounds.getYsize() != 1) test.fail("Y dimensions were not correct.");
        if(centerBounds.getZsize() != 1) test.fail("Z dimensions were not correct.");

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        GameTestAssertions.assertFalse(positions.isEmpty());
        GameTestAssertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void calculates_center_bounds_even(final GameTestHelper test) {
        AABB fullBounds = new AABB(0, 0, 0, 6, 1, 6);
        AABB centerBounds = BlockSpaceUtil.getCenterBounds(fullBounds);

        if (centerBounds.getXsize() != 2) test.fail("X dimensions were not correct.");
        if (centerBounds.getYsize() != 1) test.fail("Y dimensions were not correct.");
        if (centerBounds.getZsize() != 2) test.fail("Z dimensions were not correct");

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(centerBounds).map(BlockPos::immutable).collect(Collectors.toSet());
        if (positions.isEmpty())
            test.fail("Expected matched positions.");

        if (positions.size() != 4)
            test.fail("Expected 4 matched positions; got " + positions.size());

        GameTestAssertions.assertTrue(positions.contains(new BlockPos(2, 0, 2)));
        GameTestAssertions.assertTrue(positions.contains(new BlockPos(3, 0, 3)));

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void bounds_fits_inside(final GameTestHelper test) {
        AABB outer = new AABB(0, 0, 0, 10, 10, 10);
        AABB inner = new AABB(1, 1, 1, 3, 3, 3);

        GameTestAssertions.assertTrue(BlockSpaceUtil.boundsFitsInside(inner, outer));

        AABB unit = new AABB(0, 0, 0, 1, 1, 1);
        AABB unitX = unit.expandTowards(1, 0, 0);
        AABB unitY = unit.expandTowards(0, 1, 0);
        AABB unitZ = unit.expandTowards(0, 0, 1);

        GameTestAssertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitX, unit), "Unit should not fit in X dimension but does.");
        GameTestAssertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitY, unit), "Unit should not fit in Y dimension but does.");
        GameTestAssertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitZ, unit), "Unit should not fit in Z dimension but does.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanGetLayerBlockPositions(final GameTestHelper test) {
        AABB layer = new AABB(0, 0, 0, 5, 1, 5);

        final Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(layer)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        GameTestAssertions.assertNotNull(positions);
        GameTestAssertions.assertFalse(positions.isEmpty());

        GameTestAssertions.assertEquals(25, positions.size());

        GameTestAssertions.assertTrue(positions.contains(new BlockPos(0, 0, 0)));
        GameTestAssertions.assertTrue(positions.contains(new BlockPos(4, 0, 4)));

        GameTestAssertions.assertFalse(positions.contains(new BlockPos(0, 1, 0)));

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void SingleBlockRotatesCorrectly(final GameTestHelper test) {
        BlockPos[] singleBlock = new BlockPos[]{
                new BlockPos(1, 0, 0)
        };

        Map<BlockPos, BlockPos> newLocations = BlockSpaceUtil.rotatePositionsInPlace(singleBlock);

        BlockPos rotatedPos = newLocations.get(singleBlock[0]);

        GameTestAssertions.assertEquals(new BlockPos(1, 0, 0), rotatedPos);

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void ComplexShapeRotatesInPlaceCorrectly(final GameTestHelper test) {
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

        GameTestAssertions.assertTrue(actual.containsAll(expected));

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void ComplexShapeRotates180Correctly(final GameTestHelper test) {
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

        GameTestAssertions.assertTrue(actual.containsAll(expected));
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void doesNormalizeSingleBlockPos(final GameTestHelper test) {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        var field = new AABB(min, max);

        BlockPos actual = BlockSpaceUtil.normalizeLayerPosition(field, min);

        GameTestAssertions.assertEquals(BlockPos.ZERO, actual);
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void denormalizes_multiple_positions(final GameTestHelper test) {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = new AABB(min, max);

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
            test.fail("Expected 3 positions after normalization; got " + actual.length);

        for (int i = 0; i < 3; i++) {
            BlockPos exp = expected[i];
            BlockPos act = actual[i];
            if (!exp.equals(act))
                test.fail("Expected positions to match [" + i + "]: " + exp + " vs. " + act);
        }

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void denormalizes_single_position(final GameTestHelper test) {
        // 7x7x7 field, similar to a large field
        BlockPos min = new BlockPos(100, 0, 100);
        int largeSize = MiniaturizationFieldSize.LARGE.getSize();
        BlockPos max = min.offset(largeSize, largeSize, largeSize);

        AABB field = new AABB(min, max);

        BlockPos original = new BlockPos(100, 0, 100);

        BlockPos norm = BlockSpaceUtil.normalizeLayerPosition(field, original);

        BlockPos denorm = BlockSpaceUtil.denormalizeLayerPosition(field, norm);

        GameTestAssertions.assertEquals(original, denorm);

        test.succeed();
    }
}
