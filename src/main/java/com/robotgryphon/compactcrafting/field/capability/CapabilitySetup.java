package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilitySetup {

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        CapabilityManager.INSTANCE.register(
                IMiniaturizationFieldProvider.class,
                new MiniaturizationFieldStorage(),
                MiniaturizationFieldProvider::new);
    }
}
