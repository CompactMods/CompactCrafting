package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * A miniaturization field that can have its recipe data modified.
 */
public interface MiniaturizationRecipeHolder {

    void setRecipe(ResourceLocation id);

    void clearRecipe();
}
