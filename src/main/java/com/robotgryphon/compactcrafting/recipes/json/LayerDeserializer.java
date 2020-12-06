package com.robotgryphon.compactcrafting.recipes.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.recipes.json.loaders.FilledLayerLoader;
import com.robotgryphon.compactcrafting.recipes.json.loaders.HollowLayerLoader;
import com.robotgryphon.compactcrafting.recipes.json.loaders.ILayerLoader;
import com.robotgryphon.compactcrafting.recipes.json.loaders.MixedLayerLoader;
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
                throw new JsonParseException("Unknown layer type '" + type + "'");
        }

        try {
            return loader.createLayerFromDefinition(root);
        }

        catch(RecipeLoadingException rle) {
            return null;
        }
    }
}
