package dev.compactmods.crafting.tests.recipes.util;

import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import dev.compactmods.crafting.tests.recipes.layers.TestRecipeLayerBlocks;
import dev.compactmods.crafting.tests.util.FileHelper;
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

    public static TestRecipeLayerBlocks getLayerHarness(String filename) {
        final JsonElement data = FileHelper.INSTANCE.getJsonFromFile(filename);
        return TestRecipeLayerBlocks.CODEC
                .parse(JsonOps.INSTANCE, data)
                .getOrThrow(false, Assertions::fail);
    }

    public static CCMiniRecipeComponents getComponentsFromHarness(String filename) {
        final JsonElement data = FileHelper.INSTANCE.getJsonFromFile(filename);

        final Map<String, BlockComponent> blocks = Codec.unboundedMap(Codec.STRING, BlockComponent.CODEC)
                .fieldOf("blocks")
                .codec()
                .parse(JsonOps.INSTANCE, data)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        blocks.forEach(components::registerBlock);

        return components;
    }
}
