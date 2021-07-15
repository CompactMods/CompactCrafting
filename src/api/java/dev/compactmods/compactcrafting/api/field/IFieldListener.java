package dev.compactmods.compactcrafting.api.field;

import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;

import javax.annotation.Nullable;

public interface IFieldListener {

    default void onRecipeChanged(IMiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {}

    default void onFieldActivated(IMiniaturizationField field) {}
}
