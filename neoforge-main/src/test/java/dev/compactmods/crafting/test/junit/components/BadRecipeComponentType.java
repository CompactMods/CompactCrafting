package dev.compactmods.crafting.test.junit.components;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;

public class BadRecipeComponentType implements RecipeComponentType<IRecipeComponent> {

    @Override
    public MapCodec<IRecipeComponent> getCodec() {
        return null;
    }
}
