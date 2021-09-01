package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.network.ClientFieldUnwatchPacket;
import com.robotgryphon.compactcrafting.network.ClientFieldWatchPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import dev.compactmods.compactcrafting.api.field.IActiveWorldFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent evt) {
        if(evt.phase != TickEvent.Phase.START) return;

        ClientWorld level = Minecraft.getInstance().level;
        if(level != null) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .ifPresent(IActiveWorldFields::tickFields);
        }
    }

    @SubscribeEvent
    public static void onStartChunkTracking(final ChunkWatchEvent.Watch event) {
        final ServerPlayerEntity player = event.getPlayer();
        final ChunkPos pos = event.getPos();
        final ServerWorld level = event.getWorld();

        level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
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

        level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
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
}
