package com.robotgryphon.compactcrafting.tests.recipes;

import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.tests.recipes.util.RecipeTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MixedLayerTests {

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
        Set<String> components = recipe.getComponentKeys();
        Assertions.assertTrue(components.contains("-"));
    }
}
