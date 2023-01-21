package dev.compactmods.crafting.api.recipe.layers;

import com.mojang.serialization.Codec;

public interface RecipeLayerType<L extends IRecipeLayer> {

    Codec<L> getCodec();
}
