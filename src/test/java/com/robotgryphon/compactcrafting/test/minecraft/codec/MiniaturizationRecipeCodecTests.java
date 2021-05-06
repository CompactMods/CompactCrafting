package com.robotgryphon.compactcrafting.test.minecraft.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.tests.util.FileHelper;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

public class MiniaturizationRecipeCodecTests {

    private MiniaturizationRecipe getRecipeFromFile(String filename) {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile(filename);

        Optional<Pair<MiniaturizationRecipe, JsonElement>> loaded = MiniaturizationRecipe.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(CompactCrafting.LOGGER::info);

        if (!loaded.isPresent()) {
            Assertions.fail("Recipe did not load from file.");
            return null;
        } else {
            return loaded.get().getFirst();
        }
    }

    @Test
    void LoadsRecipeFromJson() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("layers.json");

        MiniaturizationRecipe.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    MiniaturizationRecipe recipe = res.getFirst();
                    Assertions.assertTrue(true);
                });
    }

    @Test
    void LoadsRecipeLayersCorrectly() {
        MiniaturizationRecipe recipe = getRecipeFromFile("layers.json");
        if (recipe == null) {
            Assertions.fail("No recipe was loaded.");
        } else {
            // There should only be two layers loaded from the file
            Assertions.assertEquals(2, recipe.getNumberLayers());

            Optional<IRecipeLayer> topLayer = recipe.getLayer(1);
            if (!topLayer.isPresent()) {
                Assertions.fail("No top layer loaded.");
                return;
            }

            IRecipeLayer lay = topLayer.get();
            int filledCount = lay.getNumberFilledPositions(recipe.getRecipeComponents());

            // Layer only has one spot (redstone dust)
            Assertions.assertEquals(1, filledCount);

            // Top Layer should be a redstone dust, so one 'R' component
            Map<String, Integer> componentTotals = lay.getComponentTotals(recipe.getRecipeComponents());
            Assertions.assertTrue(componentTotals.containsKey("R"), "Expected redstone component in top layer; it does not exist.");
            Assertions.assertEquals(1, componentTotals.get("R"), "Expected one redstone required in top layer.");
        }
    }

    @Test
    void MakesRoundTripThroughNbtCorrectly() {
        MiniaturizationRecipe recipe = getRecipeFromFile("layers.json");
        if (recipe == null) {
            Assertions.fail("No recipe was loaded.");
        } else {
            DataResult<INBT> dr = MiniaturizationRecipe.CODEC.encodeStart(NBTDynamicOps.INSTANCE, recipe);
            Optional<INBT> res = dr.resultOrPartial(CompactCrafting.LOGGER::error);

            INBT nbtRecipe = res.get();

            MiniaturizationRecipe rFromNbt = MiniaturizationRecipe.CODEC.decode(NBTDynamicOps.INSTANCE, nbtRecipe)
                    .getOrThrow(false, CompactCrafting.LOGGER::info)
                    .getFirst();

            // There should only be two layers loaded from the file
            Assertions.assertEquals(2, rFromNbt.getNumberLayers());

            Optional<IRecipeLayer> topLayer = rFromNbt.getLayer(1);
            if (!topLayer.isPresent()) {
                Assertions.fail("No top layer loaded.");
                return;
            }

            IRecipeLayer lay = topLayer.get();
            int filledCount = lay.getNumberFilledPositions(recipe.getRecipeComponents());

            // Layer only has one spot (redstone dust)
            Assertions.assertEquals(1, filledCount);

            // Top Layer should be a redstone dust, so one 'R' component
            Map<String, Integer> componentTotals = lay.getComponentTotals(recipe.getRecipeComponents());
            Assertions.assertTrue(componentTotals.containsKey("R"), "Expected redstone component in top layer; it does not exist.");
            Assertions.assertEquals(1, componentTotals.get("R"), "Expected one redstone required in top layer.");
        }
    }
}
