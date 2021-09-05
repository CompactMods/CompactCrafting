package dev.compactmods.crafting.tests.recipes.util;

import dev.compactmods.crafting.util.DirectionUtil;
import net.minecraft.util.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DirectionUtilTests {

    @Test
    void CanGetCrossAxis() {
        final Direction.Axis crossXAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.X);
        final Direction.Axis crossYAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.Y);
        final Direction.Axis crossZAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.Z);

        Assertions.assertEquals(Direction.Axis.X, crossZAxis);
        Assertions.assertEquals(Direction.Axis.Y, crossYAxis);
        Assertions.assertEquals(Direction.Axis.Z, crossXAxis);
    }
}
