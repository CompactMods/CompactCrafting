package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

import com.google.gson.JsonObject;
import com.robotgryphon.compactcrafting.recipes.exceptions.RecipeLoadingException;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RecipeLayerSerializer<T extends IRecipeLayer>
        extends ForgeRegistryEntry<RecipeLayerSerializer<?>> {
    /**
     * Reads a layer's data from a packet buffer.
     *
     * @param buffer The buffer to pull data from.
     */
    public T readLayerData(PacketBuffer buffer) {
        return null;
    }

    /**
     * Writes a layer's data to a packet buffer.
     *
     * @param layer  The layer to write data for.
     * @param buffer The buffer to write data to.
     */
    public void writeLayerData(T layer, PacketBuffer buffer) {

    }

    /**
     * Read a layer's data from a JSON object.
     *
     * @param json The root of a layer definition, in JSON.
     * @return
     */
    public T readLayerData(JsonObject json) throws RecipeLoadingException {
        return null;
    }
}
