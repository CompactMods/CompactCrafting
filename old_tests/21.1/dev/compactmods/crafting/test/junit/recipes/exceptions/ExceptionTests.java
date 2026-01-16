package dev.compactmods.crafting.test.junit.recipes.exceptions;

import dev.compactmods.crafting.recipes.exceptions.MiniaturizationRecipeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionTests {

    @Test
    public void canCreateRecipeException() {
        MiniaturizationRecipeException ex = new MiniaturizationRecipeException("test");
        Assertions.assertEquals("test", ex.getMessage());
    }
}
