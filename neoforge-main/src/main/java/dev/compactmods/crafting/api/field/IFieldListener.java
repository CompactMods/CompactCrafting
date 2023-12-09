package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;

import javax.annotation.Nullable;

public interface IFieldListener {

    default void onRecipeChanged(IMiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {}

    default void onFieldActivated(IMiniaturizationField field) {}

    default void onRecipeCompleted(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeCleared(IMiniaturizationField field) {}
}
