package com.robotgryphon.compactcrafting.recipes.json.layers;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;

public interface ILayerLoader {

    IRecipeLayer createLayerFromDefinition(JsonObject layer) throws RecipeLoadingException;
}
