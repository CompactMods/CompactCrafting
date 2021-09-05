package dev.compactmods.crafting.tests.recipes.exceptions;

import dev.compactmods.crafting.recipes.exceptions.MiniaturizationRecipeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionTests {

    @Test
    void canCreateRecipeException() {
        MiniaturizationRecipeException ex = new MiniaturizationRecipeException("test");

        Assertions.assertNotNull(ex);
        Assertions.assertEquals("test", ex.getMessage());
    }
}
