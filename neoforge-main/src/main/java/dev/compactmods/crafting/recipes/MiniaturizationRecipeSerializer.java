package dev.compactmods.crafting.recipes;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MiniaturizationRecipeSerializer implements RecipeSerializer<MiniaturizationRecipe> {


    @Override
    public MapCodec<MiniaturizationRecipe> codec() {
        CompactCrafting.LOGGER.debug("Loading recipe codec.");
        return MiniaturizationRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MiniaturizationRecipe> streamCodec() {
        return MiniaturizationRecipe.STREAM_CODEC;
    }
}
