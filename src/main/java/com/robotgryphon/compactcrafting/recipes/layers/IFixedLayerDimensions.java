package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.AxisAlignedBB;

public interface IFixedLayerDimensions {

    /**
     * Gets the trimmed dimensions of the given recipe layer.
     *
     * @return
     */
    AxisAlignedBB getDimensions();

}
