package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCCatalystTypes {
    public static DeferredRegister<CatalystType<?>> CATALYSTS = DeferredRegister.create((Class) CatalystType.class, CompactCrafting.MOD_ID);
    public static IForgeRegistry<CatalystType<?>> CATALYST_TYPES;

    static {
        CATALYSTS.makeRegistry("catalyst_types", () -> new RegistryBuilder<CatalystType<?>>()
                .tagFolder("catalyst_types"));
    }

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
    public static void catalystRegistration(RegistryEvent.Register<CatalystType<?>> evt) {
        CATALYST_TYPES = evt.getRegistry();
    }
}
