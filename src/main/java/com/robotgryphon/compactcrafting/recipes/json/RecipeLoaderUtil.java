package com.robotgryphon.compactcrafting.recipes.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeLoaderUtil {

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

    public static Optional<ItemStack> getItemStack(JsonObject stack) {
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, stack)
                .get()
                .ifRight(err -> CompactCrafting.LOGGER.warn("Failed to load itemstack from JSON: {}", err.message()))
                .mapLeft(Pair::getFirst)
                .left();
    }

    public static Optional<BlockState> extractComponentDefinition(String key, JsonElement definition) {
        JsonObject comp = definition.getAsJsonObject();
        return BlockState.CODEC.decode(JsonOps.INSTANCE, comp)
                .get().ifRight(error -> {
                    CompactCrafting.LOGGER.warn("Failed to process blockstate for component {}: {}", key, error.message());
                }).mapLeft(Pair::getFirst).left();
    }
}
