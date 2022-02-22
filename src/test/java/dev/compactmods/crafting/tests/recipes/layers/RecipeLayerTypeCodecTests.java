package dev.compactmods.crafting.tests.recipes.layers;

import javax.annotation.Nullable;
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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeLayerTypeCodecTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty_medium")
    public static void HandlesBadTypeIdentifier(final GameTestHelper test) {
        final DataResult<RecipeLayerType<?>> result =
                RecipeLayerTypeCodec.INSTANCE.parse(JsonOps.INSTANCE, new JsonPrimitive("compactcrafting:unknown_123"));

        if(result.error().isEmpty())
            test.fail("Expected a deserialization error.");

        test.succeed();
    }

    @Test
    void HandlesBadRecipeType() {
        final RecipeLayerType<IRecipeLayer> FAKE_TYPE = generateFakeRecipeLayerType();

        final DataResult<JsonElement> badResult = RecipeLayerTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, FAKE_TYPE);

        Assertions.assertTrue(badResult.error().isPresent());
    }

    private RecipeLayerType<IRecipeLayer> generateFakeRecipeLayerType() {
        return new RecipeLayerType<IRecipeLayer>() {
            @Override
            public RecipeLayerType<?> setRegistryName(ResourceLocation name) {
                return this;
            }

            @Nullable
            @Override
            public ResourceLocation getRegistryName() {
                return null;
            }

            @Override
            public Class<RecipeLayerType<?>> getRegistryType() {
                return CCLayerTypes.RECIPE_LAYER_TYPES.getRegistrySuperType();
            }

            @Override
            public Codec<IRecipeLayer> getCodec() {
                return Codec.unit(new FilledComponentRecipeLayer("?"));
            }
        };
    }
}
