package dev.compactmods.crafting.tests.recipes.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.recipes.components.RecipeComponentTypeCodec;
import dev.compactmods.crafting.tests.GameTestTemplates;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ComponentTypeCodecTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void testFailedDecode(final GameTestHelper test) {
        JsonElement string = JsonOps.INSTANCE.createString("compactcrafting:bad_component_type");

        DataResult<RecipeComponentType<?>> result = RecipeComponentTypeCodec.INSTANCE.parse(JsonOps.INSTANCE, string);

        if (result == null) {
            test.fail("Expected a result, got null");
            return;
        }

        if (result.error().isEmpty())
            test.fail("Expected a parsing error to be present.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void testBadEncode(final GameTestHelper test) {
        RecipeComponentType<?> badComponentType = new BadRecipeComponentType();

        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, badComponentType);

        if (result == null) {
            test.fail("Expected a result, got null");
            return;
        }

        if (result.error().isEmpty())
            test.fail("Expected an error during encode");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void testEncode(final GameTestHelper test) {
        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, ComponentRegistration.EMPTY_BLOCK_COMPONENT.get());

        if (result == null) {
            test.fail("Expected an encoding result");
            return;
        }

        if (result.error().isPresent()) {
            test.fail("Expected no errors during encode");
        }

        test.succeed();
    }
}
