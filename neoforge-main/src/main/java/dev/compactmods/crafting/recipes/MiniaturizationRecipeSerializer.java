package dev.compactmods.crafting.recipes;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.NonNull;

public class MiniaturizationRecipeSerializer implements RecipeSerializer<MiniaturizationRecipe> {

    @Override
    public @NonNull MapCodec<MiniaturizationRecipe> codec() {
        CompactCrafting.LOGGER.debug("Loading recipe codec.");
        return MiniaturizationRecipe.CODEC;
    }

    @Override
    public @NonNull StreamCodec<RegistryFriendlyByteBuf, MiniaturizationRecipe> streamCodec() {
        return MiniaturizationRecipe.STREAM_CODEC;
    }
}
