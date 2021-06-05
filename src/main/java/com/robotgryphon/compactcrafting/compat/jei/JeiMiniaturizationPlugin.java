package com.robotgryphon.compactcrafting.compat.jei;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.recipes.setup.RecipeBase;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;

@JeiPlugin
public class JeiMiniaturizationPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization_crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiMiniaturizationCraftingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(Registration.FIELD_PROJECTOR_ITEM.get(), 4),
                JeiMiniaturizationCraftingCategory.UID);

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientWorld w = Minecraft.getInstance().level;
        RecipeManager rm = w == null ? null : w.getRecipeManager();
        if(rm != null) {
            List<RecipeBase> miniRecipes = rm.getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE);
            registration.addRecipes(miniRecipes, JeiMiniaturizationCraftingCategory.UID);
        }
    }
}
