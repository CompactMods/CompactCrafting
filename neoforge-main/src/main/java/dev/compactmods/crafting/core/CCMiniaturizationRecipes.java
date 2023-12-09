package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class CCMiniaturizationRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, CompactCrafting.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, CompactCrafting.MOD_ID);

    // ================================================================================================================

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER =
            RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final ResourceLocation MINIATURIZATION_RECIPE_TYPE_ID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization_recipe");

    public static final DeferredHolder<RecipeType<?>, RecipeType<MiniaturizationRecipe>> MINIATURIZATION_RECIPE = RECIPE_TYPES.register(MINIATURIZATION_RECIPE_TYPE_ID.getPath(),
            () -> RecipeType.simple(MINIATURIZATION_RECIPE_TYPE_ID));

    // ================================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPES.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
