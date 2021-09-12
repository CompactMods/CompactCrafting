package dev.compactmods.crafting.tests.recipes.util;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.tests.world.TestBlockReader;
import org.junit.jupiter.api.Assertions;

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

    @Nullable
    public static TestBlockReader getBlockReader(String filename) {
        final MiniaturizationRecipe rec = getRecipeFromFile(filename);
        if(rec == null)
            return null;

        return TestBlockReader.fromRecipe(rec);
    }

    public static MiniaturizationRecipeComponents getComponentsFromRecipeFile(String filename) {
        final JsonElement data = FileHelper.INSTANCE.getJsonFromFile(filename);

        final Map<String, BlockComponent> blocks = Codec.unboundedMap(Codec.STRING, BlockComponent.CODEC)
                .fieldOf("components")
                .codec()
                .parse(JsonOps.INSTANCE, data)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        blocks.forEach(components::registerBlock);

        return components;
    }
}
