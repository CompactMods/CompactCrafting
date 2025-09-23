package dev.compactmods.crafting.compat;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.compat.theoneprobe.TheOneProbeCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        CompactCrafting.LOGGER.trace("Sending IMC setup to TOP and other mods.");
        if (ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();
    }
}
