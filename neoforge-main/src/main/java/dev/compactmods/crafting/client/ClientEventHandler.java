package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.field.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import dev.compactmods.crafting.core.CCAttachments;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;

import java.util.Collection;

public class ClientEventHandler {

    public static void onTick(final ClientTickEvent.Post evt) {
        final var mc = Minecraft.getInstance();
        final var player = mc.player;

        if (player == null)
            return;

        int timer = player.getData(CCAttachments.PLACEMENT_TIMER);
        if (--timer == 0) {
            player.removeData(CCAttachments.PLACEMENT_TIMER);
            player.removeData(CCAttachments.PLACEMENT_HELPERS);
        } else {
            player.setData(CCAttachments.PLACEMENT_TIMER, timer);
        }
    }

    public static void onLevelRender(final RenderLevelStageEvent.AfterParticles event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        doFieldPreviewRender(mc);

        // debugRenderSmallFieldAtZero(event, MiniaturizationFieldSize.SMALL);

//        GhostRenderer.render(CCBlocks.INACTIVE_FIELD_PROJECTOR_BLOCK.get().defaultBlockState(),
//                BlockPos.ZERO, event.getPoseStack(), 0.1f);

        mc.player.getExistingData(CCAttachments.PLACEMENT_HELPERS)
                .stream()
                .flatMap(Collection::stream)
                .forEach(p -> p.render(event.getPoseStack()));
    }

    private static void debugRenderSmallFieldAtZero(RenderLevelStageEvent.AfterParticles event, MiniaturizationFieldSize size) {
        final var zero = new MiniaturizationFieldLocation(new Vector3d().add(0.5, 0.5, 0.5), size);

        final var zeroGhost = new GhostProjectorPlacementRenderer(zero);

        zeroGhost.render(event.getPoseStack());
    }

    private static void doFieldPreviewRender(Minecraft mc) {
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        final HitResult hitResult = mc.hitResult;

        double viewDistance = 64;
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
//        mc.level.getCapability(CCCapabilities.FIELDS)
//                .ifPresent(fields -> {
//                    fields.getFields()
//                            .filter(projectors -> Vec3.atCenterOf(projectors.getCenter()).closerThan(mainCamera.getPosition(), viewDistance))
//                            .filter(projectors -> projectors.getCraftingState() == EnumCraftingState.CRAFTING)
//                            .filter(projectors -> projectors.getCurrentRecipe().isPresent())
//                            .filter(IMiniaturizationField::enabled)
//                            .forEach(projectors -> {
//                                BlockPos center = projectors.getCenter();
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
//                                final IMiniaturizationRecipe rec = projectors.getCurrentRecipe().get();
//                                final int prog = projectors.getProgress();
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

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CCRenderTypes.FIELD_PIPELINE);
    }
}
