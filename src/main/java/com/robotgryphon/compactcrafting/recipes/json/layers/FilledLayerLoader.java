package com.robotgryphon.compactcrafting.recipes.json.layers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.robotgryphon.compactcrafting.recipes.layers.impl.FilledComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

public class FilledLayerLoader implements ILayerLoader {
    @Override
    public IRecipeLayer createLayerFromDefinition(JsonObject layer) {
        if(!layer.has("component"))
            throw new JsonParseException("Filled layer definition does not have an associated component key.");

        String component = layer.get("component").getAsString();

        return new FilledComponentRecipeLayer(component);
    }
}
