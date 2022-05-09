package dev.compactmods.crafting.api.field;

import javax.annotation.Nullable;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;

public interface IFieldListener {

    default void onRecipeChanged(MiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {}

    default void onFieldActivated(MiniaturizationField field) {}

    default void onRecipeCompleted(MiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeMatched(MiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeCleared(MiniaturizationField field) {}
}
