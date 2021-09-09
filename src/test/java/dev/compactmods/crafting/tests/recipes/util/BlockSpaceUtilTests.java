package dev.compactmods.crafting.tests.recipes.util;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockSpaceUtilTests {

    @Test
    void CanSpliceSingleLayer() {
        AxisAlignedBB fullBounds = new AxisAlignedBB(0, 0, 0, 10, 10, 10);
        AxisAlignedBB slice = new AxisAlignedBB(0, 0, 0, 10, 1, 10);

        final AxisAlignedBB actual = BlockSpaceUtil.getLayerBoundsByYOffset(fullBounds, 0);

        Assertions.assertEquals(slice, actual, "Slice did not equal actual returned value.");
    }

    @Test
    void NoBlocksEqualsEmptyAxisBounds() {
        final Set<BlockPos> positions = Collections.emptySet();

        final AxisAlignedBB filledBounds = BlockSpaceUtil.getBoundsForBlocks(positions);

        Assertions.assertEquals(AxisAlignedBB.ofSize(0, 0, 0), filledBounds);
    }

    @Test
    void BSBoundsFitsInside() {
        AxisAlignedBB outer = new AxisAlignedBB(0, 0, 0, 10, 10, 10);
        AxisAlignedBB inner = new AxisAlignedBB(1, 1, 1, 3, 3, 3);

        Assertions.assertTrue(BlockSpaceUtil.boundsFitsInside(inner, outer));

        AxisAlignedBB unit = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        AxisAlignedBB unitX = unit.expandTowards(1, 0, 0);
        AxisAlignedBB unitY = unit.expandTowards(0, 1, 0);
        AxisAlignedBB unitZ = unit.expandTowards(0, 0, 1);

        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitX, unit), "Unit should not fit in X dimension but does.");
        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitY, unit), "Unit should not fit in Y dimension but does.");
        Assertions.assertFalse(BlockSpaceUtil.boundsFitsInside(unitZ, unit), "Unit should not fit in Z dimension but does.");
    }

    @Test
    void CanGetLayerBlockPositions() {
        AxisAlignedBB layer = new AxisAlignedBB(0, 0, 0, 5, 1, 5);

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
