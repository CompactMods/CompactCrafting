package dev.compactmods.crafting.api.recipe.layers;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;

public interface RecipeLayerType<L extends IRecipeLayer> {

    Codec<L> getCodec();
}
