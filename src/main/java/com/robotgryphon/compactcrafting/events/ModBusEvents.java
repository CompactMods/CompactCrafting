package com.robotgryphon.compactcrafting.events;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.compat.theoneprobe.TheOneProbeCompat;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        CompactCrafting.LOGGER.trace("Sending IMC setup to TOP and other mods.");
        if (ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();
    }
}
