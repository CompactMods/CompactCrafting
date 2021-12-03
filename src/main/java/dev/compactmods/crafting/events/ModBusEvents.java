package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        // TODO - TOP integration
//        CompactCrafting.LOGGER.trace("Sending IMC setup to TOP and other mods.");
//        if (ModList.get().isLoaded("theoneprobe"))
//            TheOneProbeCompat.sendIMC();
    }
}
