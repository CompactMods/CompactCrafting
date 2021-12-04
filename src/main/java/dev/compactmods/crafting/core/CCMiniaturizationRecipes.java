package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import dev.compactmods.crafting.recipes.setup.BaseRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class CCMiniaturizationRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CompactCrafting.MOD_ID);

    // ================================================================================================================

    public static final RegistryObject<RecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER = RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final ResourceLocation MINIATURIZATION_RECIPE_TYPE_ID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization_recipe");

    public static final BaseRecipeType<MiniaturizationRecipe> MINIATURIZATION_RECIPE_TYPE = new BaseRecipeType<>(MINIATURIZATION_RECIPE_TYPE_ID);

    // ================================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPES.register(eventBus);

        // Recipe Types (Forge Registry setup does not call this yet)
        MINIATURIZATION_RECIPE_TYPE.register();
    }
}
