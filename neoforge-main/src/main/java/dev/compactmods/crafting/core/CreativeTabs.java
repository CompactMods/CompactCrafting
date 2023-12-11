package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface CreativeTabs {

    DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CompactCrafting.MOD_ID);

    DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get(), 1))
            .title(Component.translatable("itemGroup.compactcrafting"))
            .displayItems((params, out) -> {
                out.accept(CCItems.FIELD_PROJECTOR_ITEM.get());
            })
            .build());

    static void init(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
