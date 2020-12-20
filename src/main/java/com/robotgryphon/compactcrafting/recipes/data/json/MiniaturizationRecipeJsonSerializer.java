package com.robotgryphon.compactcrafting.recipes.data.json;

import com.google.gson.*;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import com.robotgryphon.compactcrafting.util.JsonUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public abstract class MiniaturizationRecipeJsonSerializer {

    public static Optional<MiniaturizationRecipe> deserialize(JsonObject root, ResourceLocation rl) {
        if(!root.has("version")) {
            CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; no version specified.");
            return Optional.empty();
        }

        // TODO: Eventually we want to have a version spec here, but for now we're just going to require it for future updates
        int version = root.get("version").getAsInt();
        if(version <= 0) {
            CompactCrafting.LOGGER.debug("Skipping pattern loading for recipe " + rl.toString() + "; version must be at least 1.");
            return Optional.empty();
        }

        MiniaturizationRecipe recipe = new MiniaturizationRecipe(rl);

        boolean layersLoaded = loadLayers(recipe, root);
        if(!layersLoaded)
            return Optional.empty();

        // Load Components - If nothing was loaded, skip the recipe
        boolean componentsLoaded = loadComponents(recipe, root);
        if(!componentsLoaded || recipe.getNumberComponents() == 0)
            return Optional.empty();

        boolean catalystsLoaded = loadCatalyst(recipe, root);
        if(!catalystsLoaded)
            return Optional.empty();

        loadOutputs(recipe, root);

        return Optional.of(recipe);
    }

    private static boolean loadOutputs(MiniaturizationRecipe recipe, JsonObject root) {
        if(!root.has("outputs")) {
            CompactCrafting.LOGGER.warn("Warning: Recipe does not have outputs defined; not skipping here, but the recipe will not give anything!");
            return false;
        }

        JsonArray outputs = root.getAsJsonArray("outputs");
        for(JsonElement output : outputs) {
            if(!output.isJsonObject())
                continue;

            JsonObject op = output.getAsJsonObject();
            Optional<ItemStack> oStack = JsonUtil.getItemStack(op);
            if(!oStack.isPresent())
                continue;

            recipe.addOutput(oStack.get());
        }

        return true;
    }

    private static boolean loadCatalyst(MiniaturizationRecipe recipe, JsonObject root) {
        if(!root.has("catalyst")) {
            CompactCrafting.LOGGER.warn("Catalyst entry not found for recipe {}; skipping rest of recipe loading.", recipe.getId());
            return false;
        }

        JsonObject catalyst = root.getAsJsonObject("catalyst");
        Optional<ItemStack> stack = JsonUtil.getItemStack(catalyst);
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

    private static boolean loadLayers(MiniaturizationRecipe recipe, JsonObject root) {
        String recipeRegName = recipe.getId().toString();
        if(!root.has("layers")) {
            String msg = String.format("Skipping pattern loading for recipe %s; no layers defined.", recipeRegName);
            CompactCrafting.LOGGER.debug(msg);
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

        boolean allDynamic = Arrays.stream(iRecipeLayers).allMatch(layer -> layer instanceof IDynamicRecipeLayer);
        if(allDynamic) {
            if(!root.has("recipeSize"))
            {
                String msg = String.format("Cannot finish recipe definition for %s: all recipe layers are dynamic and no defined size set (recipeSize).", recipeRegName);
                CompactCrafting.LOGGER.warn(msg);
                return false;
            }

            try {
                int size = root.get("recipeSize").getAsInt();
                recipe.setFluidDimensions(AxisAlignedBB.withSizeAtOrigin(size, size, size));
            } catch (Exception e) {
                CompactCrafting.LOGGER.error("Error while trying to set fluid recipe dimensions.", e);
                return false;
            }
        }

        return true;
    }

    private static boolean loadComponents(MiniaturizationRecipe recipe, JsonObject root) {
        JsonObject components = root.get("components").getAsJsonObject();
        if(components.size() == 0) {
            throw new JsonParseException("Error: No components defined.");
        }

        for(Map.Entry<String, JsonElement> component : components.entrySet()) {
            String key = component.getKey();
            JsonElement bsElement = component.getValue();
            if(bsElement.isJsonObject()) {
                Optional<BlockState> state = JsonUtil.getBlockState(bsElement.getAsJsonObject());

                if (key.isEmpty() || !state.isPresent()) {
                    CompactCrafting.LOGGER.warn("Failed to process blockstate for component {}; definition not found.", key);
                    continue;
                }

                recipe.addComponent(key, state.get());
            } else {
                CompactCrafting.LOGGER.warn("Failed to process blockstate for component {}; not a JSON object. Cannot decode.", key);
            }
        }

        return true;
    }
}
