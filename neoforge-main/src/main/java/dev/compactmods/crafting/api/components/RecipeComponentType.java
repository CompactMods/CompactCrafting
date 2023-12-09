package dev.compactmods.crafting.api.components;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeComponent;

public interface RecipeComponentType<C extends IRecipeComponent> {
    Codec<C> getCodec();
}
