package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
//        ScreenManager.register(
//                ContainerRegistration.TEST_CONTAINER.get(),
//                TestScreen::new);
    }
}
