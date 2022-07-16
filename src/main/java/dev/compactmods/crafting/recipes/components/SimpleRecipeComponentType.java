package dev.compactmods.crafting.recipes.components;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;

public class SimpleRecipeComponentType<C extends IRecipeComponent> implements RecipeComponentType<C> {

    private final Codec<C> s;

    public SimpleRecipeComponentType(Codec<C> comp) {
        this.s = comp;
    }

    @Override
    public Codec<C> getCodec() {
        return this.s;
    }
}
