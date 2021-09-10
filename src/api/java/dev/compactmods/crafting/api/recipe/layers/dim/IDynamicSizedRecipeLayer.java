package dev.compactmods.crafting.api.recipe.layers.dim;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.util.math.AxisAlignedBB;

public interface IDynamicSizedRecipeLayer {
    /**
     * Used to update a recipe layer to change the size of the recipe base.
     * @param dimensions The new dimensions of the recipe.
     */
    void setRecipeDimensions(AxisAlignedBB dimensions);

    default void setRecipeDimensions(MiniaturizationFieldSize fieldSize) {
        int dim = fieldSize.getDimensions();
        AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, dim, 1, dim);
        setRecipeDimensions(aabb);
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    default void recalculateRequirements() {}
}
