package com.robotgryphon.compactcrafting.recipes.data.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public class LayerDeserializer implements JsonDeserializer<IRecipeLayer> {

    @Override
    public IRecipeLayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        if(!root.has("type"))
            throw new JsonParseException("Layer definition missing 'type' property.");

        String type = root.get("type").getAsString();
        ResourceLocation layerType = new ResourceLocation(type);

        if(!Registration.RECIPE_SERIALIZERS.containsKey(layerType))
        {
            CompactCrafting.LOGGER.error("Unknown layer type '" + type + "'");
            return null;
        }

        RecipeLayerSerializer<?> serializer = Registration.RECIPE_SERIALIZERS.getValue(layerType);
        try {
            return serializer.readLayerData(root);
        } catch (RecipeLoadingException e) {
            CompactCrafting.LOGGER.error("Error while reading layer data.", e);
            return null;
        }
    }
}
