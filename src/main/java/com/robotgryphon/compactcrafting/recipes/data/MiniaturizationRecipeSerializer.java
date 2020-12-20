package com.robotgryphon.compactcrafting.recipes.data;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.data.json.MiniaturizationRecipeJsonSerializer;
import com.robotgryphon.compactcrafting.recipes.data.serialization.RecipeBufferData;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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

        CompoundNBT dim = buffer.readCompoundTag();
        AxisAlignedBB dims = new AxisAlignedBB(
                0, 0, 0,
                dim.getDouble("x"),
                dim.getDouble("y"),
                dim.getDouble("z")
        );

        try {
            RecipeBufferData.readRecipeCatalysts(recipe, buffer);
            RecipeBufferData.readRecipeOutputs(recipe, buffer);
            RecipeBufferData.readComponentInfo(recipe, buffer);
        } catch (Exception e) {
            CompactCrafting.LOGGER.error(e);
        }

        int numLayers = buffer.readInt();
        recipe.setLayers(new IRecipeLayer[numLayers]);
        for(int i = 0; i < numLayers; i++) {
            ResourceLocation layerType = buffer.readResourceLocation();
            if(Registration.RECIPE_SERIALIZERS.containsKey(layerType)) {
                RecipeLayerSerializer<?> serializer = Registration.RECIPE_SERIALIZERS.getValue(layerType);
                IRecipeLayer layer = serializer.readLayerData(buffer);
                if(layer instanceof IDynamicRecipeLayer)
                    ((IDynamicRecipeLayer) layer).setRecipeDimensions(dims);

                recipe.setLayer(i, layer);
            }
        }

        try {
            recipe.setFluidDimensions(dims);
        } catch (MiniaturizationRecipeException e) {
            CompactCrafting.LOGGER.error("Unable to set fluid recipe dimensions.", e);
        }

        return recipe;
    }

    @Override
    public void write(PacketBuffer buffer, MiniaturizationRecipe recipe) {
        AxisAlignedBB dimensions = recipe.getDimensions();
        CompoundNBT dim = new CompoundNBT();
        dim.putDouble("x", dimensions.getXSize());
        dim.putDouble("y", dimensions.getYSize());
        dim.putDouble("z", dimensions.getZSize());
        buffer.writeCompoundTag(dim);

        RecipeBufferData.writeRecipeCatalysts(recipe, buffer);
        RecipeBufferData.writeRecipeOutputs(recipe, buffer);
        RecipeBufferData.writeComponentInfo(recipe, buffer);

        int numLayers = recipe.getNumberLayers();
        buffer.writeInt(numLayers);

        recipe.getLayers().forEach(layer -> {
            RecipeLayerSerializer serializer = layer.getSerializer(layer);
            if(serializer != null)
                serializer.writeLayerData(layer, buffer);
            else
                buffer.writeResourceLocation(new ResourceLocation(CompactCrafting.MOD_ID, "blank"));
        });
    }

}
