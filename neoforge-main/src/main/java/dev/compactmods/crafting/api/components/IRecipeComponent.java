package dev.compactmods.crafting.api.components;

import dev.compactmods.crafting.api.components.RecipeComponentType;

public interface IRecipeComponent {
    RecipeComponentType<?> getType();
}
