package com.robotgryphon.compactcrafting.recipes.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipeManager;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class MiniaturizationPatternLoader extends JsonReloadListener {
    public MiniaturizationPatternLoader() {
        super(new Gson(), "miniaturization");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        MiniaturizationRecipeManager.clear();

        for(ResourceLocation rl : objectIn.keySet()) {
            CompactCrafting.LOGGER.debug("Got possible recipe: " + rl.toString());
            JsonElement patternJson = objectIn.get(rl);

            JsonObject root = patternJson.getAsJsonObject();
            if(!root.has("version")) {
                CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; no version specified.");
                continue;
            }

            int version = root.get("version").getAsInt();
            if(version <= 0) {
                CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; version must be at least 1.");
                continue;
            }

            // TODO: Eventually we want to have a version spec here, but for now we're just going to require it for future updates

            if(!root.has("layers")) {
                CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; no layers defined.");
                continue;
            }

            JsonArray layers = root.get("layers").getAsJsonArray();
            LayerDeserializer layerJsonSerializer = new LayerDeserializer();
            Gson g = new GsonBuilder()
                    .registerTypeAdapter(IRecipeLayer.class, layerJsonSerializer)
                    .create();

            IRecipeLayer[] iRecipeLayers = g.fromJson(layers, IRecipeLayer[].class);
            Collections.reverse(Arrays.asList(iRecipeLayers));

            MiniaturizationRecipe recipe = new MiniaturizationRecipe();
            recipe.setLayers(iRecipeLayers);

            MiniaturizationRecipeManager.add(rl, recipe);
        }
    }
}
