package dev.compactmods.crafting.tests.recipes.data;

import com.google.gson.JsonElement;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;

public class MiniaturizationRecipeSerializerTests {

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanSerialize(final GameTestHelper test) {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");
        Assertions.assertNotNull(recipe);

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        Assertions.assertDoesNotThrow(() -> s.toNetwork(buf, recipe));

        Assertions.assertNotEquals(0, buf.readableBytes());
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanRoundTripOverNetwork(final GameTestHelper test) {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");
        recipe.setId(new ResourceLocation("compactcrafting:ender_crystal"));

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();

        Assertions.assertDoesNotThrow(() -> s.toNetwork(buf, recipe));

        Assertions.assertNotEquals(0, buf.readableBytes());

        Assertions.assertDoesNotThrow(() -> {
            MiniaturizationRecipe r = s.fromNetwork(recipe.getId(), buf);
            Assertions.assertEquals(0, buf.readableBytes(), "Buffer was not empty after read.");
            Assertions.assertNotNull(r, "Recipe did not have an identifier after network read.");
        });

    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void DoesNotSerializeBadRecipeOverNetwork(final GameTestHelper test) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
            s.toNetwork(buf, null);
        }

        catch(EncoderException ex) {
            test.succeed();
        }

        // Nothing should be written to the buffer, since the recipe failed to serialize
        test.fail("Buffer write should have failed");
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void SerializerCanDeserializeJson(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> s.fromJson(id, json.getAsJsonObject()));

        Assertions.assertEquals(id, recipe.getId());
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void SerializerHandlesJsonErrorsAppropriately(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_size_dynamic.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> s.fromJson(id, json.getAsJsonObject()));

        Assertions.assertNull(recipe);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void SerializerHandlesDecodingEmptyBuffer(final GameTestHelper test) {
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> {
            return s.fromNetwork(id, buf);
        });

        Assertions.assertNull(recipe);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void SerializerHandlesDecodingEmptyCompound(final GameTestHelper test) {
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeNbt(new CompoundTag());

        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> {
            return s.fromNetwork(id, buf);
        });

        Assertions.assertNull(recipe);
    }
}
