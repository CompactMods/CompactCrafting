package com.robotgryphon.compactcrafting.api.components;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RecipeComponentType<C extends IRecipeComponent>
        extends IForgeRegistryEntry<RecipeComponentType<?>> {
    Codec<C> getCodec();
}
