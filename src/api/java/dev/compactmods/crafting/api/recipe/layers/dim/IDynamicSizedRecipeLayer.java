package dev.compactmods.crafting.api.recipe.layers.dim;

import dev.compactmods.crafting.api.field.FieldSize;
import net.minecraft.world.phys.AABB;

public interface IDynamicSizedRecipeLayer {
    /**
     * Used to update a recipe layer to change the size of the recipe base.
     * @param dimensions The new dimensions of the recipe.
     */
    void setRecipeDimensions(AABB dimensions);

    default void setRecipeDimensions(FieldSize fieldSize) {
        int dim = fieldSize.getDimensions();
        AABB aabb = new AABB(0, 0, 0, dim, 1, dim);
        setRecipeDimensions(aabb);
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    default void recalculateRequirements() {}
}
