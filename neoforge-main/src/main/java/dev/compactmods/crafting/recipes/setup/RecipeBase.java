package dev.compactmods.crafting.recipes.setup;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public abstract class RecipeBase implements Recipe<FakeInventory> {

    /**
     * Used to check if a recipe matches current crafting inventory
     *
     * @param inv
     * @param worldIn
     */
    @Override
    public boolean matches(FakeInventory inv, Level worldIn) {
        return true;
    }

    @Override
    public ItemStack assemble(FakeInventory inv, RegistryAccess regAccess) {
        return ItemStack.EMPTY;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     *
     * @param width
     * @param height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public abstract void setId(ResourceLocation recipeId);

    @Override
    public ItemStack getResultItem(RegistryAccess regAccess) {
        return ItemStack.EMPTY;
    }
}
