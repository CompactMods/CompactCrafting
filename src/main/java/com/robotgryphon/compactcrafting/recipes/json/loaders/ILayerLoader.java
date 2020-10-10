package com.robotgryphon.compactcrafting.recipes.json.loaders;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

public interface ILayerLoader {

    IRecipeLayer createLayerFromDefinition(JsonObject layer);
}
