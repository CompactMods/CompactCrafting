package com.robotgryphon.compactcrafting.recipes.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.recipes.json.loaders.FilledLayerLoader;
import com.robotgryphon.compactcrafting.recipes.json.loaders.ILayerLoader;
import com.robotgryphon.compactcrafting.recipes.json.loaders.MixedLayerLoader;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

import java.lang.reflect.Type;
import java.util.Optional;

public class LayerDeserializer implements JsonDeserializer<IRecipeLayer> {
    @Override
    public IRecipeLayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        if(!root.has("type"))
            throw new JsonParseException("Layer definition missing 'type' property.");

        Optional<ILayerLoader> loader = Optional.empty();
        String type = root.get("type").getAsString();
        switch(type.toLowerCase()) {
            case "mixed":
                // Mixed layer type, use that loader
                loader = Optional.of(new MixedLayerLoader());
                break;

            case "filled":
            case "solid":
                // Filled layer type
                // Single or make new FilledLayerType?
                loader = Optional.of(new FilledLayerLoader());
                break;

            case "hollow":
                // Hollow layer type
                loader = Optional.empty();
                break;

            default:
                throw new JsonParseException("Unknown layer type '" + type + "'");
        }

        return loader
                .map(iLayerLoader -> iLayerLoader.createLayerFromDefinition(root))
                .orElse(null);

    }
}
