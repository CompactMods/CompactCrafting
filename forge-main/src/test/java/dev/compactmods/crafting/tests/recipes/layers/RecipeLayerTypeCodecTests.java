package dev.compactmods.crafting.tests.recipes.layers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.core.CCLayerTypes;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.RecipeLayerTypeCodec;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeLayerTypeCodecTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void HandlesBadTypeIdentifier(final GameTestHelper test) {
        final DataResult<RecipeLayerType<?>> result = RecipeLayerTypeCodec.INSTANCE
                .parse(JsonOps.INSTANCE, new JsonPrimitive("compactcrafting:unknown_123"));

        if (result.error().isEmpty())
            test.fail("Expected a deserialization error.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void HandlesBadRecipeType(final GameTestHelper test) {
        final RecipeLayerType<IRecipeLayer> FAKE_TYPE = generateFakeRecipeLayerType();

        final DataResult<JsonElement> badResult = RecipeLayerTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, FAKE_TYPE);

        GameTestAssertions.assertTrue(badResult.error().isPresent());
        test.succeed();
    }

    private static RecipeLayerType<IRecipeLayer> generateFakeRecipeLayerType() {
        return () -> Codec.unit(new FilledComponentRecipeLayer("?"));
    }
}
