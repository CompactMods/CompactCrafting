package com.robotgryphon.compactcrafting.recipes.components;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SimpleRecipeComponentType<C extends IRecipeComponent>
        extends ForgeRegistryEntry<RecipeComponentType<?>>
        implements RecipeComponentType<C> {

    private final Codec<C> s;

    public SimpleRecipeComponentType(Codec<C> comp) {
        this.s = comp;
    }

    @Override
    public Codec<C> getCodec() {
        return this.s;
    }
}
