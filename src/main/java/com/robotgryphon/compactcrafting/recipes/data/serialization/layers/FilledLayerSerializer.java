package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.layers.impl.FilledComponentRecipeLayer;
import net.minecraft.network.PacketBuffer;

public class FilledLayerSerializer extends RecipeLayerSerializer<FilledComponentRecipeLayer> {

    /**
     * Reads a layer's data from a packet buffer.
     *
     * @param buffer The buffer to pull data from.
     */
    @Override
    public FilledComponentRecipeLayer readLayerData(PacketBuffer buffer) {
        String component = buffer.readString();
        FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer(component);
        return layer;
    }

    /**
     * Writes a layer's data to a packet buffer.
     *
     * @param layer  The layer to write data for.
     * @param buffer The buffer to write data to.
     */
    @Override
    public void writeLayerData(FilledComponentRecipeLayer layer, PacketBuffer buffer) {
        String comp = layer.getComponent();
        buffer.writeResourceLocation(Registration.FILLED_LAYER_SERIALIZER.getId());
        buffer.writeString(comp);
    }
}
