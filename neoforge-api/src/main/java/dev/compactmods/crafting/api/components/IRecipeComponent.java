package dev.compactmods.crafting.api.components;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.resources.Identifier;

public interface IRecipeComponent {
    Identifier RECIPE_COMPONENTS_ID = CompactCrafting.identifier("recipe_components");

    RecipeComponentType<?> getType();
}
