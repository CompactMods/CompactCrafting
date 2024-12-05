package dev.compactmods.crafting.api.components;

import com.mojang.serialization.MapCodec;

public interface RecipeComponentType<C extends IRecipeComponent> {
    MapCodec<C> getCodec();
}
