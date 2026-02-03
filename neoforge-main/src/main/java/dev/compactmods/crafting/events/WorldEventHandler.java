package dev.compactmods.crafting.events;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.field.MiniaturizationFields;
import dev.compactmods.crafting.field.impl.ActiveWorldFields;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    @SubscribeEvent
    public static void onWorldTick(final LevelTickEvent.Pre evt) {
        evt.getLevel().getExistingData(MiniaturizationFields.ACTIVE_FIELDS).ifPresent(ActiveWorldFields::tickFields);
    }

    @SubscribeEvent
    public static void onStartChunkTracking(final ChunkWatchEvent.Watch event) {
        final ServerPlayer player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerLevel level = event.getLevel();

        // FIXME
//        level.getCapability(CCCapabilities.FIELDS)
//                .map(f -> f.getFields(pos))
//                .ifPresent(activeFields -> {
//                    activeFields.forEach(projectors -> {
//                        ClientFieldWatchPacket pkt = new ClientFieldWatchPacket(projectors);
//
//                        NetworkHandler.MAIN_CHANNEL.send(
//                                PacketDistributor.PLAYER.with(() -> player),
//                                pkt
//                        );
//                    });
//                });
    }

    @SubscribeEvent
    public static void onStopChunkTracking(final ChunkWatchEvent.UnWatch event) {
        final ServerPlayer player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerLevel level = event.getLevel();

        // FIXME
//        level.getCapability(CCCapabilities.FIELDS)
//                .map(f -> f.getFields(pos))
//                .ifPresent(activeFields -> {
//                    activeFields.forEach(projectors -> {
//                        ClientFieldUnwatchPacket pkt = new ClientFieldUnwatchPacket(projectors.getCenter());
//
//                        NetworkHandler.MAIN_CHANNEL.send(
//                                PacketDistributor.PLAYER.with(() -> player),
//                                pkt
//                        );
//                    });
//                });
    }
}
