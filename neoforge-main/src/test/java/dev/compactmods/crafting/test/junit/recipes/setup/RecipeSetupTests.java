package dev.compactmods.crafting.test.junit.recipes.setup;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.setup.BaseRecipeType;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.annotation.ForEachTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecipeSetupTests {

    @Test
    public void BaseRecipeType() {
        final ResourceLocation testId = CompactCrafting.modRL("test");
        final var type = new BaseRecipeType<>(testId);

        final String typeString = type.toString();
        if (typeString == null)
            Assertions.fail("BaseRecipeType#toString returned null value");
    }

    @Test
    public void FakeInventory() {
        FakeInventory inv = new FakeInventory();

        if (!inv.isEmpty())
            Assertions.fail("Expected inventory to be empty on creation.");

        if (!inv.getItem(0).isEmpty())
            Assertions.fail("Expected inventory to be empty on creation.");
    }
}

