package com.robotgryphon.compactcrafting.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
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
                .map(BlockPos::toImmutable)
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

    public static Map<BlockPos, String> getComponentMapFromPattern(JsonObject layer) {
        if(!layer.has("pattern"))
            return Collections.emptyMap();

        JsonArray layerPattern = layer.get("pattern").getAsJsonArray();
        int zSize = layerPattern.size();

        String[][] mappedToArray = new String[zSize][];

        for(int z = 0; z < zSize; z++) {
            JsonElement jsonElement = layerPattern.get(z);
            if(!jsonElement.isJsonArray())
                throw new JsonParseException("Mixed layer definition got a non-array in its pattern definition.");

            JsonArray el = jsonElement.getAsJsonArray();
            String[] xValues = new String[el.size()];
            for(int x = 0; x < el.size(); x++) {
                xValues[x] = el.get(x).getAsString();
            }

            mappedToArray[z] = xValues;
        }

        return convertMultiArrayToMap(mappedToArray);
    }
}
