package com.robotgryphon.compactcrafting.recipes.json.loaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.robotgryphon.compactcrafting.recipes.layers.HollowComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

public class HollowLayerLoader implements ILayerLoader {
    @Override
    public IRecipeLayer createLayerFromDefinition(JsonObject layer) {
        if(!layer.has("component"))
            throw new JsonParseException("Hollow layer definition does not have an associated component key.");

        String component = layer.get("component").getAsString();

        return new HollowComponentRecipeLayer(component);
    }
}
