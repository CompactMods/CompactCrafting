package dev.compactmods.crafting.recipes;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.CompactCrafting;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class MiniaturizationRecipeSerializer implements RecipeSerializer<MiniaturizationRecipe> {

    @Override
    public Codec<MiniaturizationRecipe> codec() {
        return MiniaturizationRecipe.CODEC;
    }

    @Override
    public MiniaturizationRecipe fromNetwork(FriendlyByteBuf buffer) {
        if(!buffer.isReadable() || buffer.readableBytes() == 0) {
            CompactCrafting.LOGGER.error("Recipe not readable from buffer.");
            return null;
        }

        try {
            final MiniaturizationRecipe recipe = buffer.readJsonWithCodec(MiniaturizationRecipe.CODEC);
            CompactCrafting.LOGGER.debug("Finished recipe read.");
            return recipe;
        }

        catch(EncoderException ex) {
            CompactCrafting.RECIPE_LOGGER.error("Error reading recipe information from network: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull MiniaturizationRecipe recipe) {
        CompactCrafting.LOGGER.debug("Sending recipe over network: {}", recipe.getRecipeIdentifier());
        buffer.writeJsonWithCodec(MiniaturizationRecipe.CODEC, recipe);
    }
}
