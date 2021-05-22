package com.robotgryphon.compactcrafting.tests.recipes.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.tests.util.FileHelper;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class RecipeTestUtil {
    public static MiniaturizationRecipe getRecipeFromFile(String filename) {
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
}
