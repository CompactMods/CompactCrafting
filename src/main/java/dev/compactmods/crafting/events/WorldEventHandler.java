package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class WorldEventHandler {

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent evt) {
        CompactCrafting.LOGGER.trace("Server started; calling previously active fields to validate themselves.");
        for (ServerWorld level : evt.getServer().getAllLevels()) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
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
    public static void onWorldTick(final TickEvent.WorldTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        evt.world.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(IActiveWorldFields::tickFields);
    }

    @SubscribeEvent
    public static void onChunkLoadStatusChanged(final ChunkEvent cEvent) {
        if (cEvent instanceof ChunkEvent.Load || cEvent instanceof ChunkEvent.Unload) {
            final Chunk chunk = (Chunk) cEvent.getChunk();
            final World level = chunk.getLevel();

            final MinecraftServer server = level.getServer();
            if (server != null) {
                server.submitAsync(new TickDelayedTask(server.getTickCount() + 10, () -> {
                    BlockPos chunkCenter = chunk.getPos()
                            .getWorldPosition()
                            .offset(8, 0, 8);

                    // Run through all the fields near the chunk and
                    level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                            .ifPresent(fields -> {
                                fields.getFields(chunk.getPos()).forEach(IMiniaturizationField::checkLoaded);
                            });
                }));
            }
        }
    }
}
