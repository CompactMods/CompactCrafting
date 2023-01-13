package dev.compactmods.crafting.tests.recipes.util;

import net.minecraft.gametest.framework.GameTestAssertException;

public interface ITestHelper {

    default void failBadRecipeType(Object o) {
        throw new GameTestAssertException("Expected an instance of MiniaturizationRecipe; got: " + o.getClass().getSimpleName());
    }
}
