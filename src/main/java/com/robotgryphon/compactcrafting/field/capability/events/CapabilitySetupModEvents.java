package com.robotgryphon.compactcrafting.field.capability.events;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilitySetupModEvents {

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        CapabilityMiniaturizationField.setup();
        CapabilityActiveWorldFields.setup();
    }
}
