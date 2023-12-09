package dev.compactmods.crafting.tests.recipes.data;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.testers.TestHelper;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MiniaturizationRecipeCodecTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void LoadsRecipeFromJson(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("test_data/data/compactcrafting/recipes/compact_walls.json");

        MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> test.succeed());
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void DoesNotFailIfNoComponentsDefined(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/warn_no_components.json");

        final var result = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, test::fail);

        final IRecipeComponents components = result.getComponents();
        if (components == null) {
            test.fail("Components were null.");
            return;
        }

        // If the recipe loaded, it should have a valid component manager
        if(components.isKnownKey("I"))
            test.fail("Recipe should not know what 'I' component is.");

        if (!components.isEmptyBlock("I"))
            test.fail("Expected components to have an empty I block.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void PartialResultIfNoOutputsEntryExists(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_outputs_entry.json");

        final var loaded = MiniaturizationRecipe.CODEC
                .parse(JsonOps.INSTANCE, json)
                .get();

        if (loaded.right().isEmpty())
            test.fail("Expected partial result but got none.");

        loaded.ifRight(partial -> {
            final String message = partial.message();
            if (!message.contains("outputs"))
                test.fail("Partial result has no outputs.");

            test.succeed();
        });
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void LoadsRecipeLayersCorrectly(final GameTestHelper test) {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeByName(test, "compact_walls").orElseThrow();

        // There should only be two layers loaded from the file
        if (2 != recipe.getNumberLayers())
            test.fail("Expected exactly 2 layers in recipe");

        Optional<IRecipeLayer> topLayer = recipe.getLayer(1);
        if (topLayer.isEmpty()) {
            test.fail("No top layer loaded.");
            return;
        }

        IRecipeLayer lay = topLayer.get();

        // Top Layer should be a redstone dust, so one 'R' component
        Map<String, Integer> componentTotals = lay.getComponentTotals();
        if (!componentTotals.containsKey("R"))
            test.fail("Expected redstone component in top layer; it does not exist.");

        if (1 != componentTotals.get("R"))
            test.fail("Expected one redstone required in top layer.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY, required = false)
    public static void LoadsCatalystCorrectly(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("compact_walls");

        final var recipe = testHelper.recipe();
        Objects.requireNonNull(recipe);

        var cat = recipe.getCatalyst();
        if (cat == null)
            test.fail("Expected recipe catalyst to exist.");

        // TODO: Add empty catalyst matcher here
//        if (cat2 == null)
//            test.fail("Expected recipe with no catalyst to be EMPTY, not null");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void MakesRoundTripThroughNbtCorrectly(final GameTestHelper test) {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeByName(test, "compact_walls").orElseThrow();
        DataResult<Tag> dr = MiniaturizationRecipe.CODEC.encodeStart(NbtOps.INSTANCE, recipe);
        Optional<Tag> res = dr.resultOrPartial(test::fail);

        Tag nbtRecipe = res.get();

        MiniaturizationRecipe rFromNbt = MiniaturizationRecipe.CODEC.parse(NbtOps.INSTANCE, nbtRecipe)
                .getOrThrow(false, test::fail);

        // There should only be two layers loaded from the file
        if (2 != rFromNbt.getNumberLayers())
            test.fail("Expected 2 layers in recipe.");

        Optional<IRecipeLayer> topLayer = rFromNbt.getLayer(1);
        if (topLayer.isEmpty()) {
            test.fail("No top layer loaded.");
        }

        IRecipeLayer lay = topLayer.get();

        // Top Layer should be a redstone dust, so one 'R' component
        Map<String, Integer> componentTotals = lay.getComponentTotals();
        if (!componentTotals.containsKey("R"))
            test.fail("Expected redstone component in top layer; it does not exist.");

        if (1 != componentTotals.get("R"))
            test.fail("Expected one redstone required in top layer.");

        test.succeed();
    }
}
