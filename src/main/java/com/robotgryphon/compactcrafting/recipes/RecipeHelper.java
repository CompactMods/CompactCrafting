package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public abstract class RecipeHelper {

    /**
     * Assumes an array is on the Z axis; meant to convert a single array to a map for collective
     * adding.
     *
     * @param array
     * @return
     */
    public static Map<BlockPos, String> convertSingleArrayToMap(String[] array, int x) {
        HashMap<BlockPos, String> map = new HashMap<>();
        for (int z = 0; z < array.length; z++) {
            String val = array[z];
            BlockPos relative = new BlockPos(x, 0, z);
            map.put(relative, val);
        }

        return map;
    }

    public static Map<BlockPos, String> convertMultiArrayToMap(String[][] array) {
        HashMap<BlockPos, String> map = new HashMap<>();

        // Loop through top level
        for (int x = 0; x < array.length; x++) {
            String[] zValues = array[x];
            map.putAll(convertSingleArrayToMap(zValues, x));
        }

        return map;
    }

}
