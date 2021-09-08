package dev.compactmods.crafting.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.render.CraftingPreviewRenderer;
import dev.compactmods.crafting.network.ClientFieldUnwatchPacket;
import dev.compactmods.crafting.network.ClientFieldWatchPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        ClientWorld level = Minecraft.getInstance().level;
        if (level != null && !Minecraft.getInstance().isPaused()) {
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

    @SubscribeEvent
    public static void onWorldRender(final RenderWorldLastEvent event) {
        final WorldRenderer renderer = event.getContext();

        final Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
            return;

        final ActiveRenderInfo mainCamera = mc.gameRenderer.getMainCamera();
        final RayTraceResult hitResult = mc.hitResult;

        double viewDistance = 64;
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.getFields()
                            .filter(field -> Vector3d.atCenterOf(field.getCenter()).closerThan(mainCamera.getPosition(), viewDistance))
                            .filter(field -> field.getCraftingState() == EnumCraftingState.CRAFTING)
                            .filter(field -> field.getCurrentRecipe().isPresent())
                            .forEach(field -> {
                                BlockPos center = field.getCenter();

                                MatrixStack stack = event.getMatrixStack();
                                stack.pushPose();
                                Vector3d projectedView = mainCamera.getPosition();
                                stack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                                stack.translate(
                                        (double) center.getX(),
                                        (double) center.getY(),
                                        (double) center.getZ()
                                );

                                CraftingPreviewRenderer.render(
                                        field.getCurrentRecipe().get(), field.getProgress(), stack,
                                        buffers, 15728880, OverlayTexture.NO_OVERLAY
                                );

                                stack.popPose();
                            });
                });
    }
}
