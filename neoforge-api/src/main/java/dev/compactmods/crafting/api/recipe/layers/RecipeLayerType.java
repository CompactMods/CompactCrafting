package dev.compactmods.crafting.api.recipe.layers;

import com.mojang.serialization.MapCodec;

public interface RecipeLayerType<L extends IRecipeLayer> {

    MapCodec<L> getCodec();
}
