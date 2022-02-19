package dev.compactmods.crafting.tests.recipes.setup;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.setup.BaseRecipeType;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.recipes.setup.RecipeBase;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

public class RecipeSetupTests {

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void BaseRecipeType(final GameTestHelper test) {
        final ResourceLocation testId = new ResourceLocation("compactcrafting", "test");
        BaseRecipeType<RecipeBase> type = new BaseRecipeType<>(testId);

        final String typeString = type.toString();
        Assertions.assertNotNull(typeString);

        Assertions.assertDoesNotThrow((Executable) type::register);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void FakeInventory(final GameTestHelper test) {
        FakeInventory inv = new FakeInventory();
        Assertions.assertNotNull(inv);

        Assertions.assertTrue(inv.isEmpty());

        Assertions.assertEquals(0, inv.getContainerSize());

        Assertions.assertEquals(ItemStack.EMPTY, inv.getItem(0));

        Assertions.assertDoesNotThrow(() -> {
            ItemStack response = inv.removeItem(0, 1);
            Assertions.assertEquals(ItemStack.EMPTY, response);
        });

        Assertions.assertDoesNotThrow(() -> {
            ItemStack response = inv.removeItemNoUpdate(0);
            Assertions.assertEquals(ItemStack.EMPTY, response);
        });

        Assertions.assertDoesNotThrow(() -> inv.setItem(0, ItemStack.EMPTY));

        Assertions.assertDoesNotThrow(inv::setChanged);

        final var overworld = ServerLifecycleHooks.getCurrentServer().overworld();
        Assertions.assertFalse(inv.stillValid(FakePlayerFactory.getMinecraft(overworld)));

        Assertions.assertDoesNotThrow(inv::clearContent);
    }
}

