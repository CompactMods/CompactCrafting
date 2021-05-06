package com.robotgryphon.compactcrafting.test.util;

import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

class RecipeLoaderUtilTest {

    @Test
    void convertsStringToMap() {
        String[][] array = getTargetArray();

        Map<BlockPos, String> map = RecipeHelper.convertMultiArrayToMap(array);
    }

    @Test
    void convertsMapToComponentCounts() {
        String[][] array = getTargetArray();

        Map<BlockPos, String> map = RecipeHelper.convertMultiArrayToMap(array);

        RecipeHelper.getComponentCounts(map);
    }

    // =========================================================================================================
    // =                                                                                                       =
    // =========================================================================================================


    private String[][] getTargetArray() {
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

    private String[][] createBaseArray(int width, int height) {
        String[][] arr = new String[height][width];
        for (int i = 0; i < height; i++)
            Arrays.fill(arr[i], "-");

        return arr;
    }

}