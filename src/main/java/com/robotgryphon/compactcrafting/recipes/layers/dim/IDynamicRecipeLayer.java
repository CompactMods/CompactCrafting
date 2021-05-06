package com.robotgryphon.compactcrafting.recipes.layers.dim;

import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;

public interface IDynamicRecipeLayer extends IRecipeLayer {
    void setRecipeDimensions(AxisAlignedBB dimensions);

    default void recalculateRequirements() {}
}
