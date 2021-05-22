package com.robotgryphon.compactcrafting.recipes.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.robotgryphon.compactcrafting.api.components.RecipeComponentType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class RecipeComponentTypeCodec implements Codec<RecipeComponentType<?>> {
    // Lifted and modified from a Forge PR #7668, temporary until Forge itself supports the Codec interface

    public static final RecipeComponentTypeCodec INSTANCE = new RecipeComponentTypeCodec();

    private RecipeComponentTypeCodec() {}

    @Override
    public <T> DataResult<Pair<RecipeComponentType<?>, T>> decode(DynamicOps<T> ops, T input) {
        IForgeRegistry<RecipeComponentType<?>> reg = RegistryManager.ACTIVE.getRegistry(ComponentRegistration.RECIPE_COMPONENTS_ID);
        return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !reg.containsKey(keyValuePair.getFirst()) ?
                DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                DataResult.success(keyValuePair.mapFirst(reg::getValue)));
    }

    @Override
    public <T> DataResult<T> encode(RecipeComponentType<?> input, DynamicOps<T> ops, T prefix) {
        ResourceLocation key = input.getRegistryName();
        if(key == null)
            return DataResult.error("Unknown registry element " + input);

        T toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge);
    }
}
