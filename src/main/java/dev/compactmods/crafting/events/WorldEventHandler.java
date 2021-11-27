package dev.compactmods.crafting.events;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.network.ClientFieldUnwatchPacket;
import dev.compactmods.crafting.network.ClientFieldWatchPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    private static final HashMap<RegistryKey<World>, LazyOptional<IActiveWorldFields>> FIELDS = new HashMap<>();
    private static final Subject<ChunkEvent> CHUNK_CHANGES;
    private static final Disposable CHUNK_LISTENER;

    static {
        CHUNK_CHANGES = PublishSubject.create();
        CHUNK_LISTENER = CHUNK_CHANGES.buffer(1, TimeUnit.SECONDS, Schedulers.newThread())
                .filter(l -> !l.isEmpty())
                .subscribe((changedChunks) -> {
                    final World level = ((Chunk) changedChunks.get(0).getChunk()).getLevel();
                    if (!FIELDS.containsKey(level.dimension())) {
                        final LazyOptional<IActiveWorldFields> cap = level.getCapability(CapabilityActiveWorldFields.FIELDS);
                        FIELDS.put(level.dimension(), cap);
                        cap.addListener((listener) -> {
                            listener.ifPresent(fields -> FIELDS.remove(fields.getLevel()));
                        });
                    }

                    FIELDS.get(level.dimension()).ifPresent(fields -> {
                        changedChunks.stream()
                                .map(ce -> ce.getChunk().getPos())
                                .flatMap(fields::getFields)
                                .forEach(IMiniaturizationField::checkLoaded);
                    });
                });
    }

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent evt) {
        CompactCrafting.LOGGER.trace("Server started; calling previously active fields to validate themselves.");
        for (ServerWorld level : evt.getServer().getAllLevels()) {
            level.getCapability(CapabilityActiveWorldFields.FIELDS)
                    .resolve()
                    .ifPresent(fields -> {
                        fields.setLevel(level);
                        fields.getFields().forEach(f -> {
                            f.setLevel(level);
                            f.checkLoaded();
                        });
                    });
        }
    }

    @SubscribeEvent
    void onServerStop(final FMLServerStoppingEvent stopping) {
        if (!CHUNK_LISTENER.isDisposed())
            CHUNK_LISTENER.dispose();
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        evt.world.getCapability(CapabilityActiveWorldFields.FIELDS)
                .ifPresent(IActiveWorldFields::tickFields);
    }

    @SubscribeEvent
    public static void onStartChunkTracking(final ChunkWatchEvent.Watch event) {
        final ServerPlayerEntity player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerWorld level = event.getWorld();

        level.getCapability(CapabilityActiveWorldFields.FIELDS)
                .map(f -> f.getFields(pos))
                .ifPresent(activeFields -> {
                    activeFields.forEach(field -> {
                        ClientFieldWatchPacket pkt = new ClientFieldWatchPacket(field);

                        NetworkHandler.MAIN_CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                pkt
                        );
                    });
                });
    }

    @SubscribeEvent
    public static void onStopChunkTracking(final ChunkWatchEvent.UnWatch event) {
        final ServerPlayerEntity player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerWorld level = event.getWorld();

        level.getCapability(CapabilityActiveWorldFields.FIELDS)
                .map(f -> f.getFields(pos))
                .ifPresent(activeFields -> {
                    activeFields.forEach(field -> {
                        ClientFieldUnwatchPacket pkt = new ClientFieldUnwatchPacket(field.getCenter());

                        NetworkHandler.MAIN_CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                pkt
                        );
                    });
                });
    }

    @SubscribeEvent
    public static void onChunkLoadStatusChanged(final ChunkEvent cEvent) {
        if (cEvent instanceof ChunkEvent.Load || cEvent instanceof ChunkEvent.Unload) {
            // CompactCrafting.LOGGER.debug("Chunk load status changed: {}", cEvent.getChunk().getPos());
            CHUNK_CHANGES.onNext(cEvent);
        }
    }
}
