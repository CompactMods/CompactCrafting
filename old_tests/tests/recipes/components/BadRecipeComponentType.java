package dev.compactmods.crafting.tests.recipes.components;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;

public class BadRecipeComponentType implements RecipeComponentType<IRecipeComponent> {

    @Override
    public Codec<IRecipeComponent> getCodec() {
        return null;
    }
}
