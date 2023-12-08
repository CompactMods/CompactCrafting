package dev.compactmods.crafting.tests.testers.component;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.tests.testers.ITestableAreaHelper;

public interface IComponentTester extends ITestableAreaHelper {

    IRecipeComponents components();

    default IRecipeBlocks blocks() {
        final var level = getLevel();
        final var testBounds = getTestBounds();
        return RecipeBlocks.create(level, components(), testBounds).normalize();
    }

}