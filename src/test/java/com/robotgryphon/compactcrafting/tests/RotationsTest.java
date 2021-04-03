package com.robotgryphon.compactcrafting.tests;

import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RotationsTest {

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
}
