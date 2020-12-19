package com.robotgryphon.compactcrafting.recipes.data.json.layers;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.impl.HollowComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

public class HollowLayerLoader implements ILayerLoader {
    @Override
    public IRecipeLayer createLayerFromDefinition(JsonObject layer) throws RecipeLoadingException {
        if(!layer.has("wall"))
            throw new RecipeLoadingException("Hollow layer definition does not have an associated component key (wall).");

        String component = layer.get("wall").getAsString();

        return new HollowComponentRecipeLayer(component);
    }
}
