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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeSetupTests {

    @GameTest(template = "empty")
    public static void BaseRecipeType(final GameTestHelper test) {
        final ResourceLocation testId = new ResourceLocation("compactcrafting", "test");
        BaseRecipeType<RecipeBase> type = new BaseRecipeType<>(testId);

        final String typeString = type.toString();
        if (typeString == null)
            test.fail("BaseRecipeType#toString returned null value");

        try {
            type.register();
            test.succeed();
        } catch (Exception ex) {
            test.fail("Failed to complete registration call.");
        }
    }

    @GameTest(template = "empty")
    public static void FakeInventory(final GameTestHelper test) {
        FakeInventory inv = new FakeInventory();

        if (!inv.isEmpty() || 0 != inv.getContainerSize())
            test.fail("Expected inventory to be empty on creation.");


        if (!inv.getItem(0).isEmpty())
            test.fail("Expected inventory to be empty on creation.");

        try {
            ItemStack i1 = inv.removeItem(0, 1);
            if (!i1.isEmpty())
                test.fail("Expected inventory to be empty.");

            ItemStack i2 = inv.removeItemNoUpdate(0);
            if (!i2.isEmpty())
                test.fail("Expected inventory to be empty.");
        } catch (Exception ex) {
            test.fail(ex.getMessage());
        }

        try {
            inv.setItem(0, ItemStack.EMPTY);
            inv.setChanged();
        } catch (Exception ex) {
            test.fail("Marking a fake inventory changed or setting an item should not throw.");
        }

        final var level = test.getLevel();
        if (inv.stillValid(FakePlayerFactory.getMinecraft(level)))
            test.fail("Players should not be able to use FakeInventory");

        try {
            inv.clearContent();
        } catch (Exception ex) {
            test.fail("Clearing fake inventory should not throw.");
        }

        test.succeed();
    }
}

