package dev.compactmods.crafting.api.field;

import javax.annotation.Nullable;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;

@SuppressWarnings("unused")
public interface IFieldListener {

    default void onRecipeChanged(IMiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {}

    default void onFieldActivated(IMiniaturizationField field) {}

    default void onRecipeCompleted(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    /**
     * Fires when a recipe begins crafting inside a field.
     * @since 2.1.0
     * @param field The field that began crafting.
     * @param recipe The recipe now crafting inside the field.
     */
    default void onRecipeStarted(IMiniaturizationField field, IMiniaturizationRecipe recipe) {}

    default void onRecipeCleared(IMiniaturizationField field) {}
}
