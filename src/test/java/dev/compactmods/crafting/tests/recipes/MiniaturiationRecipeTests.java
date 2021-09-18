package dev.compactmods.crafting.tests.recipes;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@IntegrationTestClass("recipes")
public class MiniaturiationRecipeTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
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

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void FakesFakeInventories(IntegrationTestHelper helper) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = recipe.matches(new FakeInventory(), helper.getWorld());
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

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void MatchesExactStructure(IntegrationTestHelper helper) {
        final BlockPos zero = helper.relativePos(BlockPos.ZERO).get();
        final MiniaturizationRecipe enderCrystal = RecipeTestUtil.getRecipeFromFile("recipes/ender_crystal.json");
        final IRecipeBlocks blocks = RecipeBlocks
                .create(helper.getWorld(), enderCrystal.getComponents(), enderCrystal.getDimensions().move(zero))
                .normalize();

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = enderCrystal.matches(blocks);
            Assertions.assertTrue(matched);
        });
    }
}
