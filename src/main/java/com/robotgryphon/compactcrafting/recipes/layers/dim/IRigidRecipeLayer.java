package com.robotgryphon.compactcrafting.recipes.layers.dim;

import net.minecraft.util.math.AxisAlignedBB;

public interface IRigidRecipeLayer {

    /**
     * Gets the trimmed dimensions of the given recipe layer.
     *
     * @return
     */
    AxisAlignedBB getDimensions();

}
