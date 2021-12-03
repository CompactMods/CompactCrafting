package dev.compactmods.crafting.tests.recipes.setup;

import dev.compactmods.crafting.recipes.setup.BaseRecipeType;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.recipes.setup.RecipeBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class RecipeSetupTests {

    @Test
    @Tag("minecraft")
    void BaseRecipeType() {
        final ResourceLocation testId = new ResourceLocation("compactcrafting", "test");
        BaseRecipeType<RecipeBase> type = new BaseRecipeType<>(testId);

        final String typeString = type.toString();
        Assertions.assertNotNull(typeString);

        Assertions.assertDoesNotThrow((Executable) type::register);
    }

    @Test
    @Tag("minecraft")
    void FakeInventory() {
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

        ServerWorld overworld = ServerLifecycleHooks.getCurrentServer().overworld();
        Assertions.assertFalse(inv.stillValid(FakePlayerFactory.getMinecraft(overworld)));

        Assertions.assertDoesNotThrow(inv::clearContent);
    }
}

