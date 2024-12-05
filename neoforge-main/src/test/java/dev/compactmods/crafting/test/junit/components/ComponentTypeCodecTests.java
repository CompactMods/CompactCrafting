package dev.compactmods.crafting.test.junit.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.recipes.components.RecipeComponentTypeCodec;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class ComponentTypeCodecTests {

    @Test
    public void testFailedDecode() {
        JsonElement string = JsonOps.INSTANCE.createString("compactcrafting:bad_component_type");

        DataResult<RecipeComponentType<?>> result = RecipeComponentTypeCodec.INSTANCE.parse(JsonOps.INSTANCE, string);

        if (result == null) {
            Assertions.fail("Expected a result, got null");
            return;
        }

        if (result.error().isEmpty())
            Assertions.fail("Expected a parsing error to be present.");
    }

    @Test
    public void testBadEncode() {
        RecipeComponentType<?> badComponentType = new BadRecipeComponentType();

        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, badComponentType);

        if (result == null) {
            Assertions.fail("Expected a result, got null");
            return;
        }

        if (result.error().isEmpty())
            Assertions.fail("Expected an error during encode");
    }

    @Test
    public void testEncode() {
        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, ComponentRegistration.EMPTY_BLOCK_COMPONENT.get());

        if (result == null) {
            Assertions.fail("Expected an encoding result");
            return;
        }

        if (result.error().isPresent()) {
            Assertions.fail("Expected no errors during encode");
        }
    }
}
