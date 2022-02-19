package dev.compactmods.crafting.tests.recipes.data;

import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Assertions;

public class MiniaturizationRecipeCodecTests {

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void LoadsRecipeFromJson(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("test_data/data/compactcrafting/recipes/compact_walls.json");

        MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> test.succeed());
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void RequiresRecipeSizeOnAllDynamicLayers(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_size_dynamic.json");

        Optional<DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .error();

        if(loaded.isEmpty())
            test.fail("Data did not load.");

        String error = loaded.get().message();
        if(error == null)
            test.fail("Expected an error message from codec.");

        test.succeed();
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void DoesNotFailIfNoComponentsDefined(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/warn_no_components.json");

        Either<MiniaturizationRecipe, DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        if(loaded.right().isPresent())
            test.fail("err - loaded.right");

        loaded.ifLeft(result -> {
            final IRecipeComponents components = result.getComponents();
            if(components == null)
                test.fail("Components were null.");

            // Even though the recipe loaded, it should have remapped the missing component as an empty block
            if(!components.hasBlock("I"))
                test.fail("Expected components to have added an empty I block.");

            if(!components.isEmptyBlock("I"))
                test.fail("Expected components to have an empty I block.");

            test.succeed();
        });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void PartialResultIfNoOutputsEntryExists(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_outputs_entry.json");

        final var loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        if(loaded.right().isEmpty())
            test.fail("Expected partial result but got none.");

        loaded.ifRight(partial -> {
            final String message = partial.message();
            if(!message.contains("outputs"))
                test.fail("Partial result has no outputs.");

            test.succeed();
        });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void PartialResultIfNoOutputsExist(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_outputs.json");

        Either<MiniaturizationRecipe, DataResult.PartialResult<MiniaturizationRecipe>> loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        if(loaded.left().isPresent())
            test.fail("Full result exists; expected only partial results.");

        if(loaded.right().isEmpty())
            test.fail("Expected partial result.");

        loaded.ifRight(partial -> {
            final String message = partial.message();
            if(message.contains("No outputs"))
                test.fail("Error did not mention no outputs.");

            test.succeed();
        });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void LoadsRecipeLayersCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/compact_walls.json");
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

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void LoadsCatalystCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/compact_walls.json");
        Assertions.assertNotNull(recipe);
        Assertions.assertNotNull(recipe.getCatalyst());

        MiniaturizationRecipe noComponents = RecipeTestUtil.getRecipeFromFile("recipe_tests/warn_no_catalyst.json");
        Assertions.assertNotNull(noComponents);
        Assertions.assertNull(noComponents.getCatalyst());
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void MakesRoundTripThroughNbtCorrectly() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/compact_walls.json");
        if (recipe == null) {
            Assertions.fail("No recipe was loaded.");
        } else {
            DataResult<Tag> dr = MiniaturizationRecipe.CODEC.encodeStart(NbtOps.INSTANCE, recipe);
            Optional<Tag> res = dr.resultOrPartial(Assertions::fail);

            Tag nbtRecipe = res.get();

            MiniaturizationRecipe rFromNbt = MiniaturizationRecipe.CODEC.parse(NbtOps.INSTANCE, nbtRecipe)
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
