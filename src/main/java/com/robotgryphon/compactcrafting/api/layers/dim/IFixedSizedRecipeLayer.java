package com.robotgryphon.compactcrafting.api.layers.dim;

import net.minecraft.util.math.AxisAlignedBB;

public interface IFixedSizedRecipeLayer {

    /**
     * Gets the trimmed dimensions of the given recipe layer.
     *
     * @return
     */
    AxisAlignedBB getDimensions();

}
