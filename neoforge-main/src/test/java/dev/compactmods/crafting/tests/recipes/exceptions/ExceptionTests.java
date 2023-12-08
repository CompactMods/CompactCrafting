package dev.compactmods.crafting.tests.recipes.exceptions;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.exceptions.MiniaturizationRecipeException;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ExceptionTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void canCreateRecipeException(final GameTestHelper test) {
        MiniaturizationRecipeException ex = new MiniaturizationRecipeException("test");
        GameTestAssertions.assertEquals("test", ex.getMessage());
        test.succeed();
    }
}
