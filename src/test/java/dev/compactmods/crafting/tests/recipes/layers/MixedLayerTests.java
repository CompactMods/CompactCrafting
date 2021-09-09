package dev.compactmods.crafting.tests.recipes.layers;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MixedLayerTests {

    @Test
    @Tag("minecraft")
    void CanCreateLayerInstance() {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile("layers/mixed/basic.json");

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        parsed.resultOrPartial(Assertions::fail)
                .ifPresent(layer -> {
                    Assertions.assertNotNull(layer);
                    int filled = layer.getNumberFilledPositions();

                    Assertions.assertEquals(25, filled);
                });
    }

    @Test
    @Tag("minecraft")
    void remapsUnknownComponents() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/ender_crystal.json");
        if(recipe == null)
            Assertions.fail();

        Optional<IRecipeLayer> layer = recipe.getLayer(2);
        layer.ifPresent(lay -> {
            // "-" is a component that was not found in the recipe file's component list, so it needs remapped
            Map<String, Integer> totals = lay.getComponentTotals();
            Assertions.assertTrue(totals.containsKey("-"));
        });

        // Now check if the component was remapped
        Set<String> components = recipe.getComponents().getAllComponents().keySet();
        Assertions.assertTrue(components.contains("-"));
    }
}
