package dev.compactmods.crafting.test.junit.recipes.util;

import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.test.FileHelper;

import javax.annotation.Nonnull;
import java.util.Optional;

public class JUnitTestHelper {

    @Nonnull
    public static Optional<MiniaturizationRecipe> getRecipeFromFile(String name) {
        final var data = FileHelper.getJsonFromFile(name + ".json");
        return MiniaturizationRecipe.CODEC.codec()
                .parse(JsonOps.INSTANCE, data)
                .resultOrPartial();
    }

    @Nonnull
    public static Optional<MiniaturizationRecipe> getRecipeFromTestRecipes(String name) {
        return getRecipeFromFile("test_datapacks/test_data/data/compactcrafting/recipe/" + name);
    }
}
