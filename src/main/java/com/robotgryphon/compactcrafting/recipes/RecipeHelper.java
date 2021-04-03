package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class RecipeHelper {

    public static BlockPos[] normalizeFieldPositions(MiniaturizationFieldBlockData fieldBlocks) {
        BlockPos[] filledBlocks = fieldBlocks.getFilledBlocks();
        AxisAlignedBB filledBounds = fieldBlocks.getFilledBounds();

        return Stream.of(filledBlocks)
                .parallel()
                .map(p -> BlockSpaceUtil.normalizeLayerPosition(filledBounds, p))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);
    }

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

    public static Map<String, Integer> getComponentCounts(Map<BlockPos, String> map) {
        Map<String, Integer> counts = new HashMap<>();

        map.forEach((key, value) -> {
            counts.putIfAbsent(value, 0);
            int prev = counts.get(value);
            counts.replace(value, prev + 1);
        });

        return counts;
    }

}
