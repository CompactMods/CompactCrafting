package dev.compactmods.crafting.tests.recipes.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.util.FileHelper;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class RecipeTestUtil {
    public static MiniaturizationRecipe getRecipeFromFile(String filename) {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile(filename);

        Optional<MiniaturizationRecipe> loaded = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(CompactCrafting.LOGGER::info);

        if (!loaded.isPresent()) {
            Assertions.fail("Recipe did not load from file.");
            return null;
        } else {
            return loaded.get();
        }
    }
}
