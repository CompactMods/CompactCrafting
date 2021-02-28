package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

import com.mojang.serialization.Codec;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SimpleRecipeLayerType<T extends RecipeLayer>
        extends ForgeRegistryEntry<RecipeLayerType<?>>
        implements RecipeLayerType<T> {

    private final Codec<T> s;
    public SimpleRecipeLayerType(Codec<T> s) {
        this.s = s;
    }

    @Override
    public Codec<T> getCodec() {
        return s;
    }
}
