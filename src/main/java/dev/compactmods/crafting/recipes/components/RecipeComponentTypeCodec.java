package dev.compactmods.crafting.recipes.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public final class RecipeComponentTypeCodec implements Codec<RecipeComponentType<?>> {
    // Lifted and modified from a Forge PR #7668, temporary until Forge itself supports the Codec interface

    public static final RecipeComponentTypeCodec INSTANCE = new RecipeComponentTypeCodec();

    private RecipeComponentTypeCodec() {
    }

    @Override
    public <T> DataResult<Pair<RecipeComponentType<?>, T>> decode(DynamicOps<T> ops, T input) {
        IForgeRegistry<RecipeComponentType<?>> reg = ComponentRegistration.COMPONENTS.get();
        return ResourceLocation.CODEC.decode(ops, input).flatMap(rl -> {
            ResourceLocation resource = rl.getFirst();
            if (reg.containsKey(resource))
                return DataResult.success(rl.mapFirst(reg::getValue));

            return DataResult.error("Unknown registry key: " + rl.getFirst());
        });
    }

    @Override
    public <T> DataResult<T> encode(RecipeComponentType<?> input, DynamicOps<T> ops, T prefix) {
        ResourceLocation key = ComponentRegistration.COMPONENTS.get().getKey(input);
        if (key == null)
            return DataResult.error("Unknown registry element " + input);

        T toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge);
    }
}
