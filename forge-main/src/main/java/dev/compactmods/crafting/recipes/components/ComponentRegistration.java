package dev.compactmods.crafting.recipes.components;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ComponentRegistration {

    public static final ResourceLocation RECIPE_COMPONENTS_ID = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_components");

    @SuppressWarnings("unchecked")
    public static DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS = DeferredRegister.create(RECIPE_COMPONENTS_ID, CompactCrafting.MOD_ID);

    public static Supplier<IForgeRegistry<RecipeComponentType<?>>> COMPONENTS;

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
    @SuppressWarnings("unchecked")
    public static <T> Class<T> c(Class<?> cls) {
        return (Class<T>) cls;
    }

    public static void init(IEventBus modBus) {
        RECIPE_COMPONENTS.register(modBus);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void newRegistries(final NewRegistryEvent evt) {
        final var b = new RegistryBuilder<RecipeComponentType<?>>()
                .setName(RECIPE_COMPONENTS_ID)
                .setType(c(RecipeComponentType.class));

        COMPONENTS = evt.create(b);
    }
}
