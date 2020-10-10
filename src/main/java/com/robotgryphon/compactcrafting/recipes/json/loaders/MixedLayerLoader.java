package com.robotgryphon.compactcrafting.recipes.json.loaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.MixedComponentRecipeLayer;
import com.robotgryphon.compactcrafting.util.RecipeLoaderUtil;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class MixedLayerLoader implements ILayerLoader {

    @Override
    public IRecipeLayer createLayerFromDefinition(JsonObject layer) {
        if(!layer.has("pattern"))
            throw new JsonParseException("Mixed layer definition does not have an associated pattern.");

        Map<BlockPos, String> compMap = RecipeLoaderUtil.getComponentMapFromPattern(layer);

        MixedComponentRecipeLayer mixed = new MixedComponentRecipeLayer();
        for(Map.Entry<BlockPos, String> mapping : compMap.entrySet()) {
            String comp = mapping.getValue();

            // Skip empty and dashed components, treat them as air
            if(comp.trim().isEmpty() || comp.equals("-") || comp.equals("_"))
                continue;

            mixed.add(comp, mapping.getKey());
        }

        return mixed;
    }

}
