package dev.compactmods.crafting.tests.util;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.util.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class DirectionUtilTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanGetCrossAxis(final GameTestHelper test) {
        final Direction.Axis crossXAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.X);
        final Direction.Axis crossYAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.Y);
        final Direction.Axis crossZAxis = DirectionUtil.getCrossDirectionAxis(Direction.Axis.Z);

        GameTestAssertions.assertEquals(Direction.Axis.X, crossZAxis);
        GameTestAssertions.assertEquals(Direction.Axis.Y, crossYAxis);
        GameTestAssertions.assertEquals(Direction.Axis.Z, crossXAxis);

        test.succeed();
    }
}
