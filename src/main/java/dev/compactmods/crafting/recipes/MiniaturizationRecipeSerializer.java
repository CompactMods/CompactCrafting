package dev.compactmods.crafting.recipes;

import javax.annotation.Nullable;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MiniaturizationRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MiniaturizationRecipe> {

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
    public MiniaturizationRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        boolean debugReg = ServerConfig.RECIPE_REGISTRATION.get();
        if (debugReg) CompactCrafting.LOGGER.debug("Starting recipe read: {}", recipeId);

        try {
            final MiniaturizationRecipe recipe = buffer.readWithCodec(MiniaturizationRecipe.CODEC);
            recipe.setId(recipeId);

            if(debugReg) CompactCrafting.LOGGER.debug("Finished recipe read: {}", recipeId);

            return recipe;
        } catch (IOException e) {
            CompactCrafting.LOGGER.error(String.format("Miniaturization recipe failed to decode: %s", recipeId), e);
        }

        return null;
    }

    @Override
    public void toNetwork(PacketBuffer buffer, MiniaturizationRecipe recipe) {
        boolean debugReg = ServerConfig.RECIPE_REGISTRATION.get();
        if(debugReg && recipe != null)
            CompactCrafting.LOGGER.debug("Sending recipe over network: {}", recipe.getRecipeIdentifier());

        try {
            buffer.writeWithCodec(MiniaturizationRecipe.CODEC, recipe);
        } catch (IOException ioe) {
            if(recipe != null)
                CompactCrafting.LOGGER.error(String.format("Failed to encode recipe for network: %s", recipe.getRecipeIdentifier()), ioe);
            else
                CompactCrafting.LOGGER.error("Failed to encode recipe for network.", ioe);
        }
    }
}
