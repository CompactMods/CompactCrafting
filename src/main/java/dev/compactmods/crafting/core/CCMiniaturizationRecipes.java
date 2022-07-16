package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;


public class CCMiniaturizationRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CompactCrafting.MOD_ID);

    // ================================================================================================================

    public static final RegistryObject<RecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER = RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final ResourceLocation MINIATURIZATION_RECIPE_TYPE_ID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization_recipe");

    public static final RegistryObject<RecipeType<MiniaturizationRecipe>> MINIATURIZATION_RECIPE = RECIPE_TYPES.register(MINIATURIZATION_RECIPE_TYPE_ID.getPath(),
            () -> RecipeType.simple(MINIATURIZATION_RECIPE_TYPE_ID));

    // ================================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPES.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
