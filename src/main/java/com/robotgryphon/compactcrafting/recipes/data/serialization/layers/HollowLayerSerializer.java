package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.impl.HollowComponentRecipeLayer;
import net.minecraft.network.PacketBuffer;

public class HollowLayerSerializer extends RecipeLayerSerializer<HollowComponentRecipeLayer> {

    @Override
    public HollowComponentRecipeLayer readLayerData(JsonObject json) throws RecipeLoadingException {
        if(!json.has("wall"))
            throw new RecipeLoadingException("Hollow layer definition does not have an associated component key (wall).");

        String component = json.get("wall").getAsString();

        return new HollowComponentRecipeLayer(component);
    }

    @Override
    public HollowComponentRecipeLayer readLayerData(PacketBuffer buffer) {
        String component = buffer.readString();
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer(component);
        return layer;
    }

    @Override
    public void writeLayerData(HollowComponentRecipeLayer layer, PacketBuffer buffer) {
        String comp = layer.getComponent();
        buffer.writeResourceLocation(Registration.HOLLOW_LAYER_SERIALIZER.getId());
        buffer.writeString(comp);
    }
}
