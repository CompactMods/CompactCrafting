package com.robotgryphon.compactcrafting.recipes.components;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.components.impl.BlockStateComponent;
import com.robotgryphon.compactcrafting.recipes.components.impl.EmptyBlockComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ComponentRegistration {

    public static DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS = DeferredRegister.create((Class) RecipeComponentType.class, CompactCrafting.MOD_ID);
    public static IForgeRegistry<RecipeComponentType<?>> RECIPE_COMPONENT_TYPES;

    static {
        RECIPE_COMPONENTS.makeRegistry("recipe_components", () -> new RegistryBuilder<RecipeComponentType<?>>()
                .setName(new ResourceLocation(CompactCrafting.MOD_ID, "recipe_components"))
                .setType(c(RecipeComponentType.class))
                .tagFolder("recipe_components"));
    }

    // ================================================================================================================
    //   RECIPE COMPONENTS
    // ================================================================================================================
    public static final RegistryObject<RecipeComponentType<BlockStateComponent>> BLOCKSTATE_COMPONENT =
            RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType(BlockStateComponent.CODEC));

    public static final RegistryObject<RecipeComponentType<EmptyBlockComponent>> EMPTY_BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("empty", () -> new SimpleRecipeComponentType(EmptyBlockComponent.CODEC));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    @SuppressWarnings("unchecked")
    private static <T> Class<T> c(Class<?> cls) { return (Class<T>)cls; }

    public static void init(IEventBus modBus) {
        RECIPE_COMPONENTS.register(modBus);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onComponentRegistration(RegistryEvent.Register<RecipeComponentType<?>> evt) {
        RECIPE_COMPONENT_TYPES = evt.getRegistry();
    }
}
