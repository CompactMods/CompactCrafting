package dev.compactmods.crafting.tests.recipes.data;

import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MiniaturizationRecipeCodecTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    @Tag("minecraft")
    void LoadsRecipeFromJson() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("data/compactcrafting/recipes/compact_walls.json");

        MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> Assertions.assertTrue(true));
    }

    @Test
    @Tag("minecraft")
    void RequiresRecipeSizeOnAllDynamicLayers() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("recipe_tests/fail_no_size_dynamic.json");

        Optional<DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .error();

        Assertions.assertTrue(loaded.isPresent());

        String error = loaded.get().message();
        Assertions.assertNotNull(error);
    }

    @Test
    @Tag("minecraft")
    void DoesNotFailIfNoComponentsDefined() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("recipe_tests/warn_no_components.json");

        Either<MiniaturizationRecipe, DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        Assertions.assertFalse(loaded.right().isPresent());
        loaded.ifLeft(result -> {
            final IRecipeComponents components = result.getComponents();
            Assertions.assertNotNull(components);

            // Even though the recipe loaded, it should have remapped the missing component as an empty block
            Assertions.assertTrue(components.hasBlock("I"), "Expected components to have added an empty I block.");
            Assertions.assertTrue(components.isEmptyBlock("I"), "Expected components to have an empty I block.");
        });
    }

    @Test
    @Tag("minecraft")
    void PartialResultIfNoOutputsEntryExists() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("recipe_tests/fail_no_outputs_entry.json");

        Either<MiniaturizationRecipe, DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        Assertions.assertTrue(loaded.right().isPresent());
        loaded.ifRight(partial -> {
            final String message = partial.message();
            Assertions.assertTrue(message.contains("outputs"));
        });
    }

    @Test
    @Tag("minecraft")
    void PartialResultIfNoOutputsExist() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("recipe_tests/fail_no_outputs.json");

        Either<MiniaturizationRecipe, DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        Assertions.assertFalse(loaded.left().isPresent());
        Assertions.assertTrue(loaded.right().isPresent());
        loaded.ifRight(partial -> {
            final String message = partial.message();
            Assertions.assertTrue(message.contains("No outputs"));
        });
    }

    @Test
    @Tag("minecraft")
    void LoadsRecipeLayersCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("data/compactcrafting/recipes/compact_walls.json");
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
    void LoadsCatalystCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("data/compactcrafting/recipes/compact_walls.json");
        Assertions.assertNotNull(recipe);
        Assertions.assertFalse(recipe.getCatalyst().isEmpty());

        MiniaturizationRecipe noComponents = RecipeTestUtil.getRecipeFromFile("recipe_tests/warn_no_catalyst.json");
        Assertions.assertNotNull(noComponents);
        Assertions.assertTrue(noComponents.getCatalyst().isEmpty());
    }

    @Test
    @Tag("minecraft")
    void MakesRoundTripThroughNbtCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("data/compactcrafting/recipes/compact_walls.json");
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
