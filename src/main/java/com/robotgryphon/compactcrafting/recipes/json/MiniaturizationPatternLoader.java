package com.robotgryphon.compactcrafting.recipes.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipeManager;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
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
            if(!root.has("version")) {
                CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; no version specified.");
                continue;
            }

            // TODO: Eventually we want to have a version spec here, but for now we're just going to require it for future updates
            int version = root.get("version").getAsInt();
            if(version <= 0) {
                CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; version must be at least 1.");
                continue;
            }

            MiniaturizationRecipe recipe = new MiniaturizationRecipe(rl);

            boolean layersLoaded = loadLayers(recipe, root);
            if(!layersLoaded)
                continue;

            // Load Components - If nothing was loaded, skip the recipe
            boolean componentsLoaded = loadComponents(recipe, root);
            if(!componentsLoaded || recipe.getNumberComponents() == 0)
                continue;

            boolean catalystsLoaded = loadCatalyst(recipe, root);
            if(!catalystsLoaded)
                continue;

            loadOutputs(recipe, root);

            MiniaturizationRecipeManager.add(rl, recipe);
        }
    }

    private boolean loadOutputs(MiniaturizationRecipe recipe, JsonObject root) {
        if(!root.has("outputs")) {
            CompactCrafting.LOGGER.warn("Warning: Recipe does not have outputs defined; not skipping here, but the recipe will not give anything!");
            return false;
        }

        JsonArray outputs = root.getAsJsonArray("outputs");
        for(JsonElement output : outputs) {
            if(!output.isJsonObject())
                continue;

            JsonObject op = output.getAsJsonObject();
            Optional<ItemStack> oStack = RecipeLoaderUtil.getItemStack(op);
            if(!oStack.isPresent())
                continue;

            recipe.addOutput(oStack.get());
        }

        return true;
    }

    private boolean loadCatalyst(MiniaturizationRecipe recipe, JsonObject root) {
        if(!root.has("catalyst")) {
            CompactCrafting.LOGGER.warn("Catalyst entry not found for recipe {}; skipping rest of recipe loading.", recipe.getRegistryName());
            return false;
        }

        JsonObject catalyst = root.getAsJsonObject("catalyst");
        Optional<ItemStack> stack = RecipeLoaderUtil.getItemStack(catalyst);
        if(!stack.isPresent())
            return false;
        
        ItemStack c = stack.get();

        if(c.getCount() != 1) {
            CompactCrafting.LOGGER.warn("Catalyst definition called for a non-1 count; this is not yet supported.");
            c.setCount(1);
        }

        recipe.setCatalyst(c);

        return true;
    }

    private boolean loadLayers(MiniaturizationRecipe recipe, JsonObject root) {
        if(!root.has("layers")) {
            CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + recipe.getRegistryName().toString() + "; no layers defined.");
            return false;
        }

        JsonArray layers = root.get("layers").getAsJsonArray();
        LayerDeserializer layerJsonSerializer = new LayerDeserializer();
        Gson g = new GsonBuilder()
                .registerTypeAdapter(IRecipeLayer.class, layerJsonSerializer)
                .create();

        IRecipeLayer[] iRecipeLayers = g.fromJson(layers, IRecipeLayer[].class);
        Collections.reverse(Arrays.asList(iRecipeLayers));


        recipe.setLayers(iRecipeLayers);
        return true;
    }

    private boolean loadComponents(MiniaturizationRecipe recipe, JsonObject root) {
        JsonObject components = root.get("components").getAsJsonObject();
        if(components.size() == 0) {
            throw new JsonParseException("Error: No components defined.");
        }

        for(Map.Entry<String, JsonElement> component : components.entrySet()) {
            String key = component.getKey();
            Optional<BlockState> state = RecipeLoaderUtil.extractComponentDefinition(key, component.getValue());

            if(key.isEmpty() || !state.isPresent()) {
                CompactCrafting.LOGGER.warn("Failed to process blockstate for component {}; definition not found.", key);
                continue;
            }

            recipe.addComponent(key, state.get());
        }

        return true;
    }

}
