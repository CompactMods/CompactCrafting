package dev.compactmods.crafting.tests.recipes;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MiniaturiationRecipeTests {

    private static MinecraftServer SERVER;
    private static ServerWorld OVERWORLD;

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);

        SERVER = ServerLifecycleHooks.getCurrentServer();
        OVERWORLD = SERVER.overworld();
    }

    @Test
    @Tag("minecraft")
    void CanCreate() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
    }

    @Test
    @Tag("minecraft")
    void CanSetId() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            recipe.setId(new ResourceLocation(CompactCrafting.MOD_ID, "test"));
        });
    }


    @Test
    @Tag("minecraft")
    void IsSpecialRecipe() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
        Assertions.assertTrue(recipe.isSpecial());
    }

    @Test
    @Tag("minecraft")
    void FakesFakeInventories() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = recipe.matches(new FakeInventory(), OVERWORLD);
            Assertions.assertTrue(matched);
        });
    }

    @Test
    @Tag("minecraft")
    void FakesAssemble() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            ItemStack result = recipe.assemble(new FakeInventory());
            Assertions.assertTrue(result.isEmpty());
        });
    }

    @Test
    @Tag("minecraft")
    void FakesCanCraftDimensions() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean canCraft = recipe.canCraftInDimensions(0, 0);
            Assertions.assertTrue(canCraft);
        });
    }

    @Test
    @Tag("minecraft")
    void FakesResultItem() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            final ItemStack result = recipe.getResultItem();
            Assertions.assertTrue(result.isEmpty());
        });
    }
}
