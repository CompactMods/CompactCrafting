package dev.compactmods.crafting.test.gametests.recipes.layers;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.test.gametests.TestFrameworkTemplates;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "layers")
public class RecipeLayerCodecTests {

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void HandlesBadTypeIdentifier(final GameTestHelper test) {
        JsonObject el = new JsonObject();
        el.addProperty("type", "compactcrafting:unknown_123");

        final var result = MiniaturizationRecipe.LAYER_CODEC.parse(JsonOps.INSTANCE, el);

        if (result.error().isEmpty())
            test.fail("Expected a deserialization error.");

        test.succeed();
    }
}
