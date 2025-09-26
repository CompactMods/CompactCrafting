package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import dev.compactmods.crafting.client.render.ProxyProjectorHighlighter;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final ClientTickEvent.Post evt) {
        GhostProjectorPlacementRenderer.tick();
        ProxyProjectorHighlighter.cleanupExpiredBlinking();

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && !Minecraft.getInstance().isPaused()) {
//            level.getCapability(CCCapabilities.FIELDS)
//                    .ifPresent(IActiveWorldFields::tickFields);
        }
    }

    @SubscribeEvent
    public static void onWorldRender(final RenderLevelStageEvent event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) {
            GhostProjectorPlacementRenderer.render(event.getPoseStack());
            doFieldPreviewRender(event, mc);
        }
        
        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)) {
            final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            ProxyProjectorHighlighter.renderAllBlinkingProjectors(event.getPoseStack(), buffers, mc.level);
            buffers.endBatch();
        }
        
    }

    @Nonnull
    private static void doFieldPreviewRender(RenderLevelStageEvent event, Minecraft mc) {
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        final HitResult hitResult = mc.hitResult;

        double viewDistance = 64;
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
//        mc.level.getCapability(CCCapabilities.FIELDS)
//                .ifPresent(fields -> {
//                    fields.getFields()
//                            .filter(field -> Vec3.atCenterOf(field.getCenter()).closerThan(mainCamera.getPosition(), viewDistance))
//                            .filter(field -> field.getCraftingState() == EnumCraftingState.CRAFTING)
//                            .filter(field -> field.getCurrentRecipe().isPresent())
//                            .filter(IMiniaturizationField::enabled)
//                            .forEach(field -> {
//                                BlockPos center = field.getCenter();
//
//                                PoseStack stack = event.getPoseStack();
//                                stack.pushPose();
//                                Vec3 projectedView = mainCamera.getPosition();
//                                stack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
//
//                                stack.translate(
//                                        (double) center.getX(),
//                                        (double) center.getY(),
//                                        (double) center.getZ()
//                                );
//
//                                final IMiniaturizationRecipe rec = field.getCurrentRecipe().get();
//                                final int prog = field.getProgress();
//
//                                CraftingPreviewRenderer.render(
//                                        rec, prog, stack,
//                                        buffers, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY
//                                );
//
//                                stack.popPose();
//                            });
//                });
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockPos pos = event.getPos();
        Block block = event.getLevel().getBlockState(pos).getBlock();
        
        if (block == CCBlocks.MATCH_FIELD_PROXY_BLOCK.get() || block == CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get()) {
            BlockPos fieldCenter = ClientPacketHandler.getProxyFieldCenter(pos);
            if (fieldCenter == null) {
                event.getEntity().displayClientMessage(Component.literal("Proxy is not bound to any fields !"), true);
                return;
            }
            
            var fields = event.getLevel().getData(CCAttachments.ACTIVE_FIELDS);
            fields.get(fieldCenter).ifPresent(field -> {
                boolean anyAdded = false;
                int alreadyBlinking = 0;
                
                for (BlockPos projectorPos : field.getProjectors().locations()) {
                    if (ProxyProjectorHighlighter.addBlinkingProjector(projectorPos)) {
                        anyAdded = true;
                    } else {
                        alreadyBlinking++;
                    }
                }
                
                if (anyAdded && alreadyBlinking == 0) {
                    event.getEntity().sendSystemMessage(Component.literal("Projectors highlighted for 10 seconds"));
                } else if (alreadyBlinking > 0 && !anyAdded) {
                    event.getEntity().sendSystemMessage(Component.literal("Projectors are already highlighted"));
                } else if (anyAdded && alreadyBlinking > 0) {
                    event.getEntity().sendSystemMessage(Component.literal("Some projectors highlighted for 10 seconds (others already active)"));
                }
            });
        }
    }
}
