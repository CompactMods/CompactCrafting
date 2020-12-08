package com.robotgryphon.compactcrafting.recipes.json.layers;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

import java.lang.reflect.Type;

public class LayerDeserializer implements JsonDeserializer<IRecipeLayer> {

    private static ILayerLoader MIXED = new MixedLayerLoader();
    private static ILayerLoader HOLLOW = new HollowLayerLoader();
    private static ILayerLoader FILLED = new FilledLayerLoader();

    @Override
    public IRecipeLayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        if(!root.has("type"))
            throw new JsonParseException("Layer definition missing 'type' property.");

        ILayerLoader loader;
        String type = root.get("type").getAsString();
        switch(type.toLowerCase()) {
            case "mixed":
                // Mixed layer type
                loader = MIXED;
                break;

            case "filled":
            case "solid":
                // Filled layer type
                loader = FILLED;
                break;

            case "hollow":
                // Hollow layer type
                loader = HOLLOW;
                break;

            default:
                CompactCrafting.LOGGER.error("Unknown layer type '" + type + "'");
                return null;
        }

        try {
            return loader.createLayerFromDefinition(root);
        }

        catch(RecipeLoadingException rle) {
            return null;
        }
    }
}
