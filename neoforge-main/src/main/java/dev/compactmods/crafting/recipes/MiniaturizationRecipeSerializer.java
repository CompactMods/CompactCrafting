package dev.compactmods.crafting.recipes;

import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.server.ServerConfig;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class MiniaturizationRecipeSerializer implements RecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        CompactCrafting.LOGGER.debug("Beginning deserialization of recipe: {}", recipeId.toString());
        DataResult<MiniaturizationRecipe> parseResult = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json);

        if (parseResult.error().isPresent()) {
            DataResult.PartialResult<MiniaturizationRecipe> pr = parseResult.error().get();
            CompactCrafting.RECIPE_LOGGER.error("Error loading recipe: " + pr.message());
            return null;
        }

        return parseResult.result()
                .map(r -> {
                    r.setId(recipeId);
                    return r;
                })
                .orElse(null);
    }

    @Nullable
    @Override
    public MiniaturizationRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        CompactCrafting.LOGGER.debug("Starting recipe read: {}", recipeId);

        if(!buffer.isReadable() || buffer.readableBytes() == 0) {
            CompactCrafting.LOGGER.error("Recipe not readable from buffer: {}", recipeId);

            return null;
        }

        try {
            final MiniaturizationRecipe recipe = buffer.readWithCodec(MiniaturizationRecipe.CODEC);
            recipe.setId(recipeId);

            CompactCrafting.LOGGER.debug("Finished recipe read: {}", recipeId);

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
        buffer.writeWithCodec(MiniaturizationRecipe.CODEC, recipe);
    }
}
