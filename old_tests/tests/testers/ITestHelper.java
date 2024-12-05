package dev.compactmods.crafting.tests.testers;

import net.minecraft.gametest.framework.GameTestAssertException;

public interface ITestHelper {

    default void failBadRecipeType(Object o) {
        throw new GameTestAssertException("Expected an instance of MiniaturizationRecipe; got: " + o.getClass().getSimpleName());
    }
}