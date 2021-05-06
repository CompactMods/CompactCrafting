package com.robotgryphon.compactcrafting.recipes.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RecipeComponentType<C extends RecipeComponent> extends IForgeRegistryEntry<RecipeComponentType<?>> {
    // Lifted and modified from a Forge PR #7668, temporary until Forge itself supports the Codec interface
    Codec<RecipeComponentType> CODEC = new Codec<RecipeComponentType>() {
        @Override
        public <T> DataResult<Pair<RecipeComponentType, T>> decode(DynamicOps<T> ops, T input) {
            return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !Registration.getRecipeComponentTypes().containsKey(keyValuePair.getFirst()) ?
                    DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                    DataResult.success(keyValuePair.mapFirst(Registration.getRecipeComponentTypes()::getValue)));
        }

        @Override
        public <T> DataResult<T> encode(RecipeComponentType input, DynamicOps<T> ops, T prefix) {
            ResourceLocation key = input.getRegistryName();
            if(key == null)
                return DataResult.error("Unknown registry element " + input);

            T toMerge = ops.createString(key.toString());
            return ops.mergeToPrimitive(prefix, toMerge);
        }
    };

    Codec<C> getCodec();
}
