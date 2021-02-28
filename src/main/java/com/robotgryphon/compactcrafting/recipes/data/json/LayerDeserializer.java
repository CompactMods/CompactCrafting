package com.robotgryphon.compactcrafting.recipes.data.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public class LayerDeserializer implements JsonDeserializer<RecipeLayerType> {

    @Override
    public RecipeLayerType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        if(!root.has("type"))
            throw new JsonParseException("Layer definition missing 'type' property.");

        String type = root.get("type").getAsString();
        ResourceLocation layerType = new ResourceLocation(type);

        if(!Registration.RECIPE_LAYER_TYPES.containsKey(layerType))
        {
            CompactCrafting.LOGGER.error("Unknown layer type '" + type + "'");
            return null;
        }

        RecipeLayerType<?> serializer = Registration.RECIPE_LAYER_TYPES.getValue(layerType);
        // TODO - This moves to the CODEC system now
//        try {
//            return serializer.getCodec().parse(root);
//        } catch (RecipeLoadingException e) {
//            CompactCrafting.LOGGER.error("Error while reading layer data.", e);
//            return null;
//        }
        return null;
    }
}
