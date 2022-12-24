package dev.compactmods.crafting.tests.recipes.util;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.RecipeHelper;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.Map;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeLoaderUtilTest {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void convertsStringToMap(final GameTestHelper test) {
        String[][] array = getTargetArray();

        Map<BlockPos, String> map = RecipeHelper.convertMultiArrayToMap(array);

        assertPosition(map, 1, 1);
        assertPosition(map, 1, 2);
        assertPosition(map, 1, 3);

        assertPosition(map, 2, 1);
        assertPosition(map, 2, 3);

        assertPosition(map, 3, 1);
        assertPosition(map, 3, 2);
        assertPosition(map, 3, 3);

        test.succeed();
    }

    private static void assertPosition(Map<BlockPos, String> map, int x, int z) {
        GameTestAssertions.assertEquals("X", getPosition(map, x, z));
    }

    private static String getPosition(Map<BlockPos, String> map, int x, int z) {
        return map.get(new BlockPos(x, 0, z));
    }

    // =========================================================================================================
    // =                                                                                                       =
    // =========================================================================================================


    private static String[][] getTargetArray() {
        String[][] array = createBaseArray(5, 5);
        array[1][1] = "X";
        array[1][2] = "X";
        array[1][3] = "X";

        array[2][1] = "X";
        array[2][3] = "X";

        array[3][1] = "X";
        array[3][2] = "X";
        array[3][3] = "X";
        return array;
    }

    private static String[][] createBaseArray(int width, int height) {
        String[][] arr = new String[height][width];
        for (int i = 0; i < height; i++)
            Arrays.fill(arr[i], "-");

        return arr;
    }

}