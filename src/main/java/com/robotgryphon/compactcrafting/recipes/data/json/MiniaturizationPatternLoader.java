package com.robotgryphon.compactcrafting.recipes.data.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipeManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
            Optional<MiniaturizationRecipe> recipe = MiniaturizationRecipeJsonSerializer.deserialize(root, rl);
            recipe.ifPresent(r -> MiniaturizationRecipeManager.add(rl, r));
        }

        Collection<MiniaturizationRecipe> loaded = MiniaturizationRecipeManager.getAll();
        CompactCrafting.LOGGER.info("Done reloading data; got {} recipes.", loaded.size());
    }

}
