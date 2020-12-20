package com.robotgryphon.compactcrafting.recipes.data;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.data.json.MiniaturizationRecipeJsonSerializer;
import com.robotgryphon.compactcrafting.recipes.data.util.RecipeBufferDataUtil;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Optional;

public class MiniaturizationRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, JsonObject json) {
        Optional<MiniaturizationRecipe> attempt = MiniaturizationRecipeJsonSerializer.deserialize(json, recipeId);
        return attempt.orElse(null);
    }

    @Nullable
    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe(recipeId);

        try {
            RecipeBufferDataUtil.readRecipeCatalysts(recipe, buffer);
            RecipeBufferDataUtil.readRecipeOutputs(recipe, buffer);
            RecipeBufferDataUtil.readComponentInfo(recipe, buffer);
        } catch (Exception e) {
            CompactCrafting.LOGGER.error(e);
        }

        return recipe;
    }

    @Override
    public void write(PacketBuffer buffer, MiniaturizationRecipe recipe) {
        RecipeBufferDataUtil.writeRecipeCatalysts(recipe, buffer);
        RecipeBufferDataUtil.writeRecipeOutputs(recipe, buffer);
        RecipeBufferDataUtil.writeComponentInfo(recipe, buffer);
    }

}
