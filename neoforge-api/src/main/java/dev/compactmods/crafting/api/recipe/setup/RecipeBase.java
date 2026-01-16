package dev.compactmods.crafting.api.recipe.setup;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
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
    default ItemStack assemble(FakeInventory fakeInventory, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     *
     * @param width
     * @param height
     */
    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
}
