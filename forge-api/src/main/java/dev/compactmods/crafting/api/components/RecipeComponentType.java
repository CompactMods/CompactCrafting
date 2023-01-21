package dev.compactmods.crafting.api.components;

import com.mojang.serialization.Codec;

public interface RecipeComponentType<C extends IRecipeComponent> {
    Codec<C> getCodec();
}
