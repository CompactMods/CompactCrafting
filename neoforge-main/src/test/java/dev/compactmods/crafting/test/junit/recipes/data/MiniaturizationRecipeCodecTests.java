package dev.compactmods.crafting.test.junit.recipes.data;

import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.test.junit.recipes.util.JUnitTestHelper;
import dev.compactmods.crafting.test.FileHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class MiniaturizationRecipeCodecTests {

    @Test
    public void LoadsRecipeFromJson(final MinecraftServer server) {
        JsonElement json = FileHelper.getJsonFromFile("test_datapacks/test_data/data/compactcrafting/recipe/compact_walls.json");

        MiniaturizationRecipe.CODEC.codec()
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresentOrElse(res -> {}, Assertions::fail);
    }

    @Test
    public void DoesNotFailIfNoComponentsDefined(final MinecraftServer server) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/warn_no_components.json");

        final var result = MiniaturizationRecipe.CODEC
                .codec()
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .orElseThrow();

        final IRecipeComponents components = result.getComponents();
        if (components == null) {
            Assertions.fail("Components were null.");
            return;
        }

        // If the recipe loaded, it should have a valid component manager
        if (components.isKnownKey("I"))
            Assertions.fail("Recipe should not know what 'I' component is.");

        if (!components.isEmptyBlock("I"))
            Assertions.fail("Expected components to have an empty I block.");
    }

    @Test
    public void LoadsRecipeLayersCorrectly(final MinecraftServer server) {
        MiniaturizationRecipe recipe = JUnitTestHelper.getRecipeFromTestRecipes("compact_walls").orElseThrow();

        // There should only be two layers loaded from the file
        if (2 != recipe.getNumberLayers())
            Assertions.fail("Expected exactly 2 layers in recipe");

        Optional<IRecipeLayer> topLayer = recipe.getLayer(1);
        if (topLayer.isEmpty()) {
            Assertions.fail("No top layer loaded.");
            return;
        }

        IRecipeLayer lay = topLayer.get();

        // Top Layer should be a redstone dust, so one 'R' component
        Map<String, Integer> componentTotals = lay.getComponentTotals();
        if (!componentTotals.containsKey("R"))
            Assertions.fail("Expected redstone component in top layer; it does not exist.");

        if (1 != componentTotals.get("R"))
            Assertions.fail("Expected one redstone required in top layer.");
    }

    @Test
    public void MakesRoundTripThroughNbtCorrectly(final MinecraftServer server) {
        MiniaturizationRecipe recipe = JUnitTestHelper.getRecipeFromTestRecipes("compact_walls").orElseThrow();
        DataResult<Tag> dr = MiniaturizationRecipe.CODEC.codec()
                .encodeStart(NbtOps.INSTANCE, recipe);

        Optional<Tag> res = dr.resultOrPartial(Assertions::fail);

        Tag nbtRecipe = res.get();

        MiniaturizationRecipe rFromNbt = MiniaturizationRecipe.CODEC.codec()
                .parse(NbtOps.INSTANCE, nbtRecipe)
                .getOrThrow();

        // There should only be two layers loaded from the file
        if (2 != rFromNbt.getNumberLayers())
            Assertions.fail("Expected 2 layers in recipe.");

        Optional<IRecipeLayer> topLayer = rFromNbt.getLayer(1);
        if (topLayer.isEmpty()) {
            Assertions.fail("No top layer loaded.");
        }

        IRecipeLayer lay = topLayer.get();

        // Top Layer should be a redstone dust, so one 'R' component
        Map<String, Integer> componentTotals = lay.getComponentTotals();
        if (!componentTotals.containsKey("R"))
            Assertions.fail("Expected redstone component in top layer; it does not exist.");

        if (1 != componentTotals.get("R"))
            Assertions.fail("Expected one redstone required in top layer.");
    }
}
