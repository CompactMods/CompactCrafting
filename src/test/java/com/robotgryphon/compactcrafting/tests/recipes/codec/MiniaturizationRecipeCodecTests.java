package com.robotgryphon.compactcrafting.tests.recipes.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.tests.recipes.util.RecipeTestUtil;
import com.robotgryphon.compactcrafting.tests.util.FileHelper;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

public class MiniaturizationRecipeCodecTests {

    @Test
    @Tag("minecraft")
    void LoadsRecipeFromJson() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("recipes/layers.json");

        MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> Assertions.assertTrue(true));
    }

    @Test
    @Tag("minecraft")
    void RequiresRecipeSizeOnAllDynamicLayers() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/no_size_dynamic.json");

        // TODO
    }

    @Test
    @Tag("minecraft")
    void LoadsRecipeLayersCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/layers.json");
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

            // Top Layer should be a redstone dust, so one 'R' component
            Map<String, Integer> componentTotals = lay.getComponentTotals();
            Assertions.assertTrue(componentTotals.containsKey("R"), "Expected redstone component in top layer; it does not exist.");
            Assertions.assertEquals(1, componentTotals.get("R"), "Expected one redstone required in top layer.");
        }
    }

    @Test
    @Tag("minecraft")
    void MakesRoundTripThroughNbtCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/layers.json");
        if (recipe == null) {
            Assertions.fail("No recipe was loaded.");
        } else {
            DataResult<INBT> dr = MiniaturizationRecipe.CODEC.encodeStart(NBTDynamicOps.INSTANCE, recipe);
            Optional<INBT> res = dr.resultOrPartial(Assertions::fail);

            INBT nbtRecipe = res.get();

            MiniaturizationRecipe rFromNbt = MiniaturizationRecipe.CODEC.parse(NBTDynamicOps.INSTANCE, nbtRecipe)
                    .getOrThrow(false, Assertions::fail);

            // There should only be two layers loaded from the file
            Assertions.assertEquals(2, rFromNbt.getNumberLayers());

            Optional<IRecipeLayer> topLayer = rFromNbt.getLayer(1);
            if (!topLayer.isPresent()) {
                Assertions.fail("No top layer loaded.");
                return;
            }

            IRecipeLayer lay = topLayer.get();

            // Top Layer should be a redstone dust, so one 'R' component
            Map<String, Integer> componentTotals = lay.getComponentTotals();
            Assertions.assertTrue(componentTotals.containsKey("R"), "Expected redstone component in top layer; it does not exist.");
            Assertions.assertEquals(1, componentTotals.get("R"), "Expected one redstone required in top layer.");
        }
    }
}
