package com.robotgryphon.compactcrafting.tests.recipes.components;

import com.mojang.serialization.Codec;
import dev.compactmods.compactcrafting.api.components.IRecipeComponent;
import dev.compactmods.compactcrafting.api.components.RecipeComponentType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BadRecipeComponentType implements RecipeComponentType<IRecipeComponent> {

    @Override
    public Codec<IRecipeComponent> getCodec() {
        return null;
    }

    @Override
    public RecipeComponentType<?> setRegistryName(ResourceLocation name) {
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return null;
    }

    @Override
    public Class<RecipeComponentType<?>> getRegistryType() {
        return null;
    }
}
