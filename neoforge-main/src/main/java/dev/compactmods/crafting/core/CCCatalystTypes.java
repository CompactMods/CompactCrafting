package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CCCatalystTypes {

    public static final ResourceLocation CATALYSTS_RL = new ResourceLocation(CompactCrafting.MOD_ID, "catalyst_types");

    public static final ResourceKey<Registry<CatalystType<?>>> CATALYSTS_REG_KEY = ResourceKey.createRegistryKey(CATALYSTS_RL);
    public static DeferredRegister<CatalystType<?>> CATALYSTS = DeferredRegister.create(CATALYSTS_REG_KEY, CompactCrafting.MOD_ID);

    public static Registry<CatalystType<?>> CATALYST_TYPES = CATALYSTS.makeRegistry(b -> {});

    // ================================================================================================================

    public static final DeferredHolder<CatalystType<?>, ItemStackCatalystMatcher> ITEM_STACK_CATALYST =
            CATALYSTS.register("item", () -> new ItemStackCatalystMatcher());

    public static final DeferredHolder<CatalystType<?>, ItemTagCatalystMatcher> TAGGED_ITEM_CATALYST =
            CATALYSTS.register("tag", () -> new ItemTagCatalystMatcher());

    public static void init(IEventBus bus) {
        CATALYSTS.register(bus);
    }

    // ================================================================================================================
}
