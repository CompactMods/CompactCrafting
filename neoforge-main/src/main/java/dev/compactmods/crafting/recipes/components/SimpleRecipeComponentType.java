package dev.compactmods.crafting.recipes.components;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;

public class SimpleRecipeComponentType<C extends IRecipeComponent> implements RecipeComponentType<C> {

    private final MapCodec<C> s;

    public SimpleRecipeComponentType(MapCodec<C> comp) {
        this.s = comp;
    }

    @Override
    public MapCodec<C> getCodec() {
        return this.s;
    }
}
