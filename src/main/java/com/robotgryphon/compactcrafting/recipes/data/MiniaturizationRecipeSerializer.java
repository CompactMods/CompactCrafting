package com.robotgryphon.compactcrafting.recipes.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.data.serialization.RecipeBufferData;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class MiniaturizationRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, JsonObject json) {
        return MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .result()
                .orElse(null);
    }

    @Nullable
    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        CompactCrafting.LOGGER.debug("Starting recipe read: {}", recipeId);
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

        CompactCrafting.LOGGER.debug("Done loading recipe meta, starting layer loading.");

        try {
            int numLayers = buffer.readInt();
            recipe.setLayers(new RecipeLayer[numLayers]);
            for (int i = 0; i < numLayers; i++) {
                // TODO - NO
//                ResourceLocation layerType = buffer.readResourceLocation();
//                if (Registration.RECIPE_SERIALIZERS.containsKey(layerType)) {
//                    RecipeLayerSerializer<?> serializer = Registration.RECIPE_SERIALIZERS.getValue(layerType);
//                    RecipeLayerType layer = serializer.readLayerData(buffer);
//                    if (layer instanceof IDynamicRecipeLayer)
//                        ((IDynamicRecipeLayer) layer).setRecipeDimensions(dims);
//
//                    recipe.setLayer(i, layer);
//                }
            }
        }

        catch(Exception e) {
            CompactCrafting.LOGGER.error("Error loading layers.", e);
        }

        try {
            /*
             * If all layers in the recipe are dynamically-sized, set the dimensions based on
             * what they are on the layer spec from the server (would be loaded from JSON)
             */
            if(recipe.getLayers().allMatch(l -> l instanceof IDynamicRecipeLayer))
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
            if(layer == null) {
                buffer.writeResourceLocation(new ResourceLocation(CompactCrafting.MOD_ID, "blank"));
                return;
            }

            // TODO - JsonOps
//            Codec<?> serializer = layer.getCodec();
//            if(serializer != null)
//                serializer.writeLayerData(layer, buffer);
//            else
//                buffer.writeResourceLocation(new ResourceLocation(CompactCrafting.MOD_ID, "blank"));
        });
    }

}
