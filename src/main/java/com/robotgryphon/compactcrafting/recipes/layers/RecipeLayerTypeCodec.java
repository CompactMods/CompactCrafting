package com.robotgryphon.compactcrafting.recipes.layers;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.robotgryphon.compactcrafting.Registration;
import dev.compactmods.compactcrafting.api.recipe.layers.RecipeLayerType;
import net.minecraft.util.ResourceLocation;

public final class RecipeLayerTypeCodec implements Codec<RecipeLayerType<?>> {
    // Lifted and modified from a Forge PR #7668, temporary until Forge itself supports the Codec interface
    public static final Codec<RecipeLayerType<?>> INSTANCE = new RecipeLayerTypeCodec();

    private RecipeLayerTypeCodec() {}

    @Override
    public <T> DataResult<Pair<RecipeLayerType<?>, T>> decode(DynamicOps<T> ops, T input) {
        return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !Registration.RECIPE_LAYER_TYPES.containsKey(keyValuePair.getFirst()) ?
                DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                DataResult.success(keyValuePair.mapFirst(Registration.RECIPE_LAYER_TYPES::getValue)));
    }

    @Override
    public <T> DataResult<T> encode(RecipeLayerType<?> input, DynamicOps<T> ops, T prefix) {
        ResourceLocation key = input.getRegistryName();
        if(key == null)
            return DataResult.error("Unknown registry element " + input);

        T toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge);
    }
}
