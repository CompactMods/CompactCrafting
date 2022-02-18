package dev.compactmods.crafting.tests.recipes.data;

import com.google.gson.JsonElement;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MiniaturizationRecipeSerializerTests {

    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    void CanSerialize() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");
        Assertions.assertNotNull(recipe);

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        Assertions.assertDoesNotThrow(() -> s.toNetwork(buf, recipe));

        Assertions.assertNotEquals(0, buf.readableBytes());
    }

    @Test
    void CanRoundTripOverNetwork() {
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

    @Test
    void DoesNotSerializeBadRecipeOverNetwork() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        Assertions.assertDoesNotThrow(() -> {
            MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
            s.toNetwork(buf, null);
        });

        // Nothing should be written to the buffer, since the recipe failed to serialize
        Assertions.assertEquals(0, buf.readableBytes());
    }

    @Test
    void SerializerCanDeserializeJson() {
        JsonElement json = FileHelper.getJsonFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> s.fromJson(id, json.getAsJsonObject()));

        Assertions.assertEquals(id, recipe.getId());
    }

    @Test
    void SerializerHandlesJsonErrorsAppropriately() {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_size_dynamic.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> s.fromJson(id, json.getAsJsonObject()));

        Assertions.assertNull(recipe);
    }

    @Test
    void SerializerHandlesDecodingEmptyBuffer() {
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        final MiniaturizationRecipe recipe = Assertions.assertDoesNotThrow(() -> {
            return s.fromNetwork(id, buf);
        });

        Assertions.assertNull(recipe);
    }

    @Test
    void SerializerHandlesDecodingEmptyCompound() {
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
