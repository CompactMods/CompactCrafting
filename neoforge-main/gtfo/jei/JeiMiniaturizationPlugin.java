package dev.compactmods.crafting.compat.jei;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class JeiMiniaturizationPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return CompactCrafting.modRL("miniaturization_crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ClientLevel w = Minecraft.getInstance().level;
        registration.addRecipeCategories(new JeiMiniaturizationCraftingCategory(registration.getJeiHelpers().getGuiHelper(), w.registryAccess()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get(), 4),
                JeiMiniaturizationCraftingCategory.RECIPE_TYPE);

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel w = Minecraft.getInstance().level;
        RecipeManager rm = w == null ? null : w.getRecipeManager();
        if(rm != null) {
            final var miniRecipes = rm.getAllRecipesFor(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            
            registration.addRecipes(JeiMiniaturizationCraftingCategory.RECIPE_TYPE, miniRecipes);
        }
    }
}
