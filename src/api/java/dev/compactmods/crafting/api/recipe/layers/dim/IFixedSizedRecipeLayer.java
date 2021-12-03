package dev.compactmods.crafting.api.recipe.layers.dim;

import net.minecraft.world.phys.AABB;

public interface IFixedSizedRecipeLayer {

    /**
     * Gets the trimmed dimensions of the given recipe layer.
     *
     * @return
     */
    AABB getDimensions();

}
