package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.AxisAlignedBB;

public interface IDynamicRecipeLayer {
    /**
     * Used to update a recipe layer to change the size of the recipe base.
     * @param dimensions The new dimensions of the recipe.
     */
    void setRecipeDimensions(AxisAlignedBB dimensions);

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    void recalculateRequirements();
}
