package com.robotgryphon.compactcrafting.recipes.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.api.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.components.impl.BlockComponent;
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
    public static final RegistryObject<RecipeComponentType<BlockComponent>> BLOCKSTATE_COMPONENT =
            RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType(BlockComponent.CODEC));

    public static final RegistryObject<RecipeComponentType<EmptyBlockComponent>> EMPTY_BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("empty", () -> new SimpleRecipeComponentType(EmptyBlockComponent.CODEC));

    // Lifted and modified from a Forge PR #7668, temporary until Forge itself supports the Codec interface
    public static final Codec<RecipeComponentType> RECIPE_TYPE_CODEC = new Codec<RecipeComponentType>() {
        @Override
        public <T> DataResult<Pair<RecipeComponentType, T>> decode(DynamicOps<T> ops, T input) {
            return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !ComponentRegistration.RECIPE_COMPONENT_TYPES.containsKey(keyValuePair.getFirst()) ?
                    DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                    DataResult.success(keyValuePair.mapFirst(ComponentRegistration.RECIPE_COMPONENT_TYPES::getValue)));
        }

        @Override
        public <T> DataResult<T> encode(RecipeComponentType input, DynamicOps<T> ops, T prefix) {
            ResourceLocation key = input.getRegistryName();
            if(key == null)
                return DataResult.error("Unknown registry element " + input);

            T toMerge = ops.createString(key.toString());
            return ops.mergeToPrimitive(prefix, toMerge);
        }
    };

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
