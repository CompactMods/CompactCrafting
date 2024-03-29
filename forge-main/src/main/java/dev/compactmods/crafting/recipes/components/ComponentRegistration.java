package dev.compactmods.crafting.recipes.components;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ComponentRegistration {

    public static final ResourceLocation RECIPE_COMPONENTS_ID = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_components");

    @SuppressWarnings("unchecked")
    public static DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS = DeferredRegister.create(RECIPE_COMPONENTS_ID, CompactCrafting.MOD_ID);

    public static Supplier<IForgeRegistry<RecipeComponentType<?>>> COMPONENTS = RECIPE_COMPONENTS.makeRegistry(() -> new RegistryBuilder<RecipeComponentType<?>>()
            .setName(RECIPE_COMPONENTS_ID));

    // ================================================================================================================
    //   RECIPE COMPONENTS
    // ================================================================================================================
    public static final RegistryObject<RecipeComponentType<BlockComponent>> BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType<>(BlockComponent.CODEC));

    public static final RegistryObject<RecipeComponentType<EmptyBlockComponent>> EMPTY_BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("empty", () -> new SimpleRecipeComponentType<>(EmptyBlockComponent.CODEC));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================

    public static void init(IEventBus modBus) {
        RECIPE_COMPONENTS.register(modBus);
    }
}
