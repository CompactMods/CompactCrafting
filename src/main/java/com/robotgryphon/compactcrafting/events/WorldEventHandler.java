package com.robotgryphon.compactcrafting.events;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IActiveWorldFields;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent evt) {
        CompactCrafting.LOGGER.trace("Server started; calling previously active fields to validate themselves.");
        for(ServerWorld level : evt.getServer().getAllLevels()) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .resolve()
                    .map(IActiveWorldFields::getFields)
                    .ifPresent(fieldStream -> {
                        fieldStream.forEach(f -> {
                            f.checkLoaded(level);
                            if(f.isLoaded())
                                f.markFieldChanged(level);
                        });
                    });
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent evt) {
        if(evt.phase != TickEvent.Phase.START) return;

        evt.world.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(f -> f.tickFields(evt.world));
    }
}
