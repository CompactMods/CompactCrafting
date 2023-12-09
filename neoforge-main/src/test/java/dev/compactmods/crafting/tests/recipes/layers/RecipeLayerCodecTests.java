package dev.compactmods.crafting.tests.recipes.layers;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.GameTestTemplates;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeLayerCodecTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void HandlesBadTypeIdentifier(final GameTestHelper test) {
        JsonObject el = new JsonObject();
        el.addProperty("type", "compactcrafting:unknown_123");

        final var result = MiniaturizationRecipe.LAYER_CODEC.parse(JsonOps.INSTANCE, el);

        if (result.error().isEmpty())
            test.fail("Expected a deserialization error.");

        test.succeed();
    }
}
