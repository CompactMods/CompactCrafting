package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilitySetup {

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        CapabilityManager.INSTANCE.register(
                IMiniaturizationField.class,
                new MiniaturizationFieldStorage(),
                MiniaturizationField::new);
    }
}
