package dev.compactmods.compactcrafting.api.field;

import javax.annotation.Nullable;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;

public interface IFieldListener {

    default void onRecipeChanged(IMiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {}

    default void onFieldActivated(IMiniaturizationField field) {}

    default void onRecipeCompleted(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeCleared(IMiniaturizationField field) {}
}
