package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.field.ActiveWorldFields;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.network.ClientFieldWatchPacket;
import dev.compactmods.crafting.network.ClientFieldUnwatchPacket;
import dev.compactmods.crafting.network.FieldActivatedPacket;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    public static final Subject<ChunkEvent> CHUNK_CHANGES;

    static {
        CHUNK_CHANGES = PublishSubject.create();
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        CompactCrafting.LOGGER.trace("Server started; calling previously active fields to validate themselves.");
        for (ServerLevel level : evt.getServer().getAllLevels()) {
            level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.getFields().forEach(f -> {
                    if (f instanceof MiniaturizationField mf) {
                        mf.checkLoaded();
                        // Send field activation to all players tracking the chunk
                        ChunkPos chunkPos = new ChunkPos(mf.getCenter());
                        FieldActivatedPacket packet = new FieldActivatedPacket(mf, mf.serverData());
                        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
                    }
                });
            });
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final LevelTickEvent.Pre evt) {
        evt.getLevel().getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(ActiveWorldFields::tickFields);
    }

    @SubscribeEvent
    public static void onStartChunkTracking(final ChunkWatchEvent.Watch event) {
        final ServerPlayer player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerLevel level = event.getLevel();

        level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
            fields.getFields(pos).forEach(field -> {
                ClientFieldWatchPacket pkt = new ClientFieldWatchPacket(field);
                PacketDistributor.sendToPlayer(player, pkt);
            });
        });
    }

    @SubscribeEvent
    public static void onStopChunkTracking(final ChunkWatchEvent.UnWatch event) {
        final ServerPlayer player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerLevel level = event.getLevel();

        level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
            fields.getFields(pos).forEach(field -> {
                ClientFieldUnwatchPacket pkt = new ClientFieldUnwatchPacket(field.getCenter());
                PacketDistributor.sendToPlayer(player, pkt);
            });
        });
    }

    @SubscribeEvent
    public static void chunkLoaded(final ChunkEvent.Load cEvent) {
        CHUNK_CHANGES.onNext(cEvent);
    }

    @SubscribeEvent
    public static void chunkUnloaded(final ChunkEvent.Unload cEvent) {
        CHUNK_CHANGES.onNext(cEvent);
    }
    
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            // Send all active fields in the player's view distance
            level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.getFields().forEach(field -> {
                    if (field instanceof MiniaturizationField mf) {
                        // Check if player can see this field's chunk
                        ChunkPos fieldChunk = new ChunkPos(mf.getCenter());
                        if (player.getChunkTrackingView().contains(fieldChunk.x, fieldChunk.z)) {
                            FieldActivatedPacket packet = new FieldActivatedPacket(mf, mf.serverData());
                            PacketDistributor.sendToPlayer(player, packet);
                        }
                    }
                });
            });
        }
    }
}
