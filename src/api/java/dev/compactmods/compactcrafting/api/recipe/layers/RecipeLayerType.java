package dev.compactmods.compactcrafting.api.recipe.layers;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RecipeLayerType<L extends IRecipeLayer> extends IForgeRegistryEntry<RecipeLayerType<?>> {

    Codec<L> getCodec();
}
