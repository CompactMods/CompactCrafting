package dev.compactmods.crafting.api.recipe.setup;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.Level;

public interface RecipeBase extends Recipe<FakeInventory> {

    /**
     * Used to check if a recipe matches current crafting inventory
     *
     * @param inv
     * @param worldIn
     */
    @Override
    default boolean matches(FakeInventory inv, Level worldIn) {
        return true;
    }

    @Override
    default ItemStack assemble(FakeInventory fakeInventory) {
        return ItemStack.EMPTY;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
}
