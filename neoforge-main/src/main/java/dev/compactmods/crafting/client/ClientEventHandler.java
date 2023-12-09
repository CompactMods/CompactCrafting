package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.TickEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
//            player.getCapability(CCCapabilities.TEMP_PROJECTOR_RENDERING)
//                    .ifPresent(IProjectorRenderInfo::tick);
        }

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
            doProjectorRender(event, mc);
            doFieldPreviewRender(event, mc);
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

    private static void doProjectorRender(RenderLevelStageEvent event, Minecraft mc) {
//        mc.player.getCapability(CCCapabilities.TEMP_PROJECTOR_RENDERING)
//                .ifPresent(render -> render.render(event.getPoseStack()));
    }
}
