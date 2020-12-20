package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RecipeLayerSerializer<T extends IRecipeLayer> extends ForgeRegistryEntry<RecipeLayerSerializer<?>> {
    /**
     * Reads a layer's data from a packet buffer.
     *
     * @param buffer The buffer to pull data from.
     */
    public T readLayerData(PacketBuffer buffer) { return null; }

    /**
     * Writes a layer's data to a packet buffer.
     *
     * @param layer  The layer to write data for.
     * @param buffer The buffer to write data to.
     */
    public void writeLayerData(T layer, PacketBuffer buffer) {}
}
