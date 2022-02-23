package dev.compactmods.crafting.tests.recipes.data;

import java.util.Objects;
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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MiniaturizationRecipeSerializerTests {

    @GameTest(template = "empty")
    public static void CanSerialize(final GameTestHelper test) {

        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeByName(test, "ender_crystal").orElse(null);
        Objects.requireNonNull(recipe);

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        s.toNetwork(buf, recipe);

        if (0 == buf.readableBytes())
            test.fail("Expected recipe to have serialized into network buffer. 0 bytes available.");

        test.succeed();
    }

    @GameTest(template = "empty")
    public static void CanRoundTripOverNetwork(final GameTestHelper test) {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeByName(test, "ender_crystal").orElseThrow();
        recipe.setId(new ResourceLocation("compactcrafting:ender_crystal"));

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();


        try {
            s.toNetwork(buf, recipe);
            if (buf.readableBytes() == 0)
                test.fail("Recipe did not properly write to network buffer.");

        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        try {
            MiniaturizationRecipe r = s.fromNetwork(recipe.getId(), buf);
            if(0 != buf.readableBytes())
                test.fail("Buffer was not empty after read.");

            if(r == null || r.getId() == null)
                test.fail("Recipe did not load correctly, or did not have an identifier after network read.");
        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty")
    public static void SerializerCanDeserializeJson(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("test_data/data/compactcrafting/recipes/ender_crystal.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        final MiniaturizationRecipe recipe = s.fromJson(id, json.getAsJsonObject());
        if (!id.equals(recipe.getId()))
            test.fail("Expected recipe ID to match");

        test.succeed();
    }

    @GameTest(template = "empty")
    public static void SerializerHandlesJsonErrorsAppropriately(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("recipe_tests/fail_no_size_dynamic.json");

        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");
        final MiniaturizationRecipe recipe = s.fromJson(id, json.getAsJsonObject());

        if (recipe != null)
            test.fail("Expected recipe to be null.");

        test.succeed();
    }

    @GameTest(template = "empty")
    public static void SerializerHandlesDecodingEmptyBuffer(final GameTestHelper test) {
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        final MiniaturizationRecipe recipe = s.fromNetwork(id, buf);
        if(recipe != null)
            test.fail("Managed to get a recipe instance from an empty network buffer");

        test.succeed();
    }

    @GameTest(template = "empty")
    public static void SerializerHandlesDecodingEmptyCompound(final GameTestHelper test) {
        MiniaturizationRecipeSerializer s = new MiniaturizationRecipeSerializer();
        final ResourceLocation id = new ResourceLocation(CompactCrafting.MOD_ID, "test");

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeNbt(new CompoundTag());

        try {
            final MiniaturizationRecipe recipe = s.fromNetwork(id, buf);
            if (recipe != null)
                test.fail("");
        } catch (Exception e) {
            test.fail("Serializer should not have failed.");
        }

        test.succeed();
    }
}
