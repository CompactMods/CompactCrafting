package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    public static final Subject<ChunkEvent> CHUNK_CHANGES;

    static {
        CHUNK_CHANGES = PublishSubject.create();
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        CompactCrafting.LOGGER.trace("Server started; calling previously active fields to validate themselves.");
//        for (ServerLevel level : evt.getServer().getAllLevels()) {
//            // FIXME
////            level.getCapability(CCCapabilities.FIELDS)
////                    .resolve()
////                    .ifPresent(fields -> {
////                        fields.setLevel(level);
////                        fields.getFields().forEach(f -> {
////                            f.setLevel(level);
////                            f.checkLoaded();
////                        });
////                    });
//        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        // FIXME
//        evt.level.getCapability(CCCapabilities.FIELDS)
//                .ifPresent(IActiveWorldFields::tickFields);
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
//                    activeFields.forEach(field -> {
//                        ClientFieldWatchPacket pkt = new ClientFieldWatchPacket(field);
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
//                    activeFields.forEach(field -> {
//                        ClientFieldUnwatchPacket pkt = new ClientFieldUnwatchPacket(field.getCenter());
//
//                        NetworkHandler.MAIN_CHANNEL.send(
//                                PacketDistributor.PLAYER.with(() -> player),
//                                pkt
//                        );
//                    });
//                });
    }

    @SubscribeEvent
    public static void chunkLoaded(final ChunkEvent.Load cEvent) {
        CHUNK_CHANGES.onNext(cEvent);
    }

    @SubscribeEvent
    public static void chunkUnloaded(final ChunkEvent.Unload cEvent) {
        CHUNK_CHANGES.onNext(cEvent);
    }
}
