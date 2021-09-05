package dev.compactmods.crafting.field.capability.events;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.capability.CapabilityFieldListener;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilitySetupModEvents {

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        CapabilityMiniaturizationField.setup();
        CapabilityActiveWorldFields.setup();
        CapabilityFieldListener.setup();
    }
}
