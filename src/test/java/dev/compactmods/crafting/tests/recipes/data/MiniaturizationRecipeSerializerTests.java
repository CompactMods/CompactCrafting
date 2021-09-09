package dev.compactmods.crafting.tests.recipes.data;

import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MiniaturizationRecipeSerializerTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    @Tag("minecraft")
    void CanSerialize() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/ender_crystal.json");

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        Assertions.assertDoesNotThrow(() -> {
            MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
            s.toNetwork(buf, recipe);
        });

        Assertions.assertNotEquals(0, buf.readableBytes());
    }

    @Test
    @Tag("minecraft")
    void CanRoundTripOverNetwork() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/ender_crystal.json");
        recipe.setId(new ResourceLocation("compactcrafting:ender_crystal"));

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
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
    @Tag("minecraft")
    void DoesNotSerializeBadRecipeOverNetwork() {

        // the things I do to test scenarios that should never happen
//        ObfuscationReflectionHelper.setPrivateValue(MiniaturizationRecipe.class, recipe, null, "outputs");

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        Assertions.assertDoesNotThrow(() -> {
            MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
            s.toNetwork(buf, null);
        });

        // Nothing should be written to the buffer, since the recipe failed to serialize
        Assertions.assertEquals(0, buf.readableBytes());
    }
}
