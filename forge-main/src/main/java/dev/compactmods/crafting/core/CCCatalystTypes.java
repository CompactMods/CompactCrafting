package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import static dev.compactmods.crafting.recipes.components.ComponentRegistration.c;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCCatalystTypes {

    public static final ResourceLocation CATALYSTS_RL = new ResourceLocation(CompactCrafting.MOD_ID, "catalyst_types");

    public static DeferredRegister<CatalystType<?>> CATALYSTS = DeferredRegister.create(CATALYSTS_RL, CompactCrafting.MOD_ID);

    public static IForgeRegistry<CatalystType<?>> CATALYST_TYPES;

    // ================================================================================================================

    public static final RegistryObject<CatalystType<ItemStackCatalystMatcher>> ITEM_STACK_CATALYST =
            CATALYSTS.register("item", ItemStackCatalystMatcher::new);

    public static final RegistryObject<CatalystType<ItemTagCatalystMatcher>> TAGGED_ITEM_CATALYST =
            CATALYSTS.register("tag", ItemTagCatalystMatcher::new);

    public static void init(IEventBus bus) {
        CATALYSTS.register(bus);
    }

    // ================================================================================================================

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void newRegistries(final NewRegistryEvent evt) {
        final var b = new RegistryBuilder<RecipeComponentType<?>>()
                .setName(CATALYSTS_RL)
                .setType(c(CatalystType.class));

        evt.create(b);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void catalystRegistration(RegistryEvent.Register<CatalystType<?>> evt) {
        CATALYST_TYPES = evt.getRegistry();
    }
}
