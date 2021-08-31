package com.robotgryphon.compactcrafting.tests.recipes.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.compactcrafting.api.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.components.ComponentRegistration;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponentTypeCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ComponentTypeCodecTests {

    @Test
    @Tag("minecraft")
    void testFailedDecode() {
        JsonElement string = JsonOps.INSTANCE.createString("compactcrafting:bad_component_type");

        DataResult<RecipeComponentType<?>> result = RecipeComponentTypeCodec.INSTANCE.parse(JsonOps.INSTANCE, string);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.error().isPresent());
    }

    @Test
    @Tag("minecraft")
    void testBadEncode() {
        RecipeComponentType<?> badComponentType = new BadRecipeComponentType();

        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, badComponentType);

        Assertions.assertNotNull(result);

        Assertions.assertTrue(result.error().isPresent());
    }

    @Test
    @Tag("minecraft")
    void testEncode() {
        DataResult<JsonElement> result = RecipeComponentTypeCodec.INSTANCE.encodeStart(JsonOps.INSTANCE, ComponentRegistration.EMPTY_BLOCK_COMPONENT.get());

        Assertions.assertNotNull(result);

        Assertions.assertFalse(result.error().isPresent());
    }
}
