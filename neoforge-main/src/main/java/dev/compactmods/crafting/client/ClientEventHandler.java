package dev.compactmods.crafting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.client.render.CCRenderPipelines;
import dev.compactmods.crafting.client.render.projector.FieldProjectorColors;
import dev.compactmods.crafting.client.render.projector.FieldProjectorRenderer;
import dev.compactmods.crafting.projector.FieldProjectorsCommon;
import dev.compactmods.crafting.projector.model.ActiveProjectorSpecialRenderer;
import dev.compactmods.crafting.projector.model.FieldProjectorDishModel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.SpecialBlockModelWrapper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockModelsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientEventHandler {

    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers evt) {
         evt.registerBlockEntityRenderer(FieldProjectorsCommon.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
    }

    public static void registerSpecialRenderers(final RegisterSpecialModelRendererEvent special) {
        special.register(CompactCrafting.identifier("projector_dish"), ActiveProjectorSpecialRenderer.Unbaked.MAP_CODEC);
    }

    public static void registerBlockModels(final RegisterBlockModelsEvent models) {
        final var wrapper = new SpecialBlockModelWrapper.Unbaked<>(
                new ActiveProjectorSpecialRenderer.Unbaked(),
                Optional.empty()
        );

        models.register(wrapper, FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.get());
    }

    public static void registerStandaloneModels(final ModelEvent.RegisterStandalone evt) {
        var unbaked = SimpleUnbakedStandaloneModel
                .blockStateModel(CompactCraftingClient.PROJECTOR_DISH_MODEL_ID);

        evt.register(CompactCraftingClient.PROJECTOR_DISH_MODEL_KEY, unbaked);
    }

    public static void registerBlockTintSources(final RegisterColorHandlersEvent.BlockTintSources colors) {
        colors.register(List.of(new FieldProjectorColors.Block()), FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.get());
    }

    public static void afterClientTick(final ClientTickEvent.Post evt) {
        final var mc = Minecraft.getInstance();
        final var player = mc.player;

        if (player == null)
            return;

        int timer = player.getData(FieldProjectorsCommon.PLACEMENT_TIMER);
        if (--timer == 0) {
            player.removeData(FieldProjectorsCommon.PLACEMENT_TIMER);
            player.removeData(FieldProjectorsCommon.PLACEMENT_HELPERS);
        } else {
            player.setData(FieldProjectorsCommon.PLACEMENT_TIMER, timer);
        }
    }

    public static void afterParticlesRender(final RenderLevelStageEvent.AfterOpaqueBlocks event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        renderActiveMiniaturizationFields(mc);
        renderPlacementPreviews(event, mc);
    }

    private static void renderPlacementPreviews(RenderLevelStageEvent.AfterOpaqueBlocks event, Minecraft mc) {
        final var renderState = event.getLevelRenderState();
        final var cameraPos = renderState.cameraRenderState.pos;

        Objects.requireNonNull(mc.player);

        final int timeLeft = mc.player.getData(FieldProjectorsCommon.PLACEMENT_TIMER);
        if (timeLeft == 0)
            return;

        final var alpha = Mth.clamp(timeLeft / 160f, 0.05f, 1f);

        final var pose = new PoseStack();
        pose.translate(Vec3.ZERO.subtract(cameraPos));

        mc.player.getExistingData(FieldProjectorsCommon.PLACEMENT_HELPERS)
                .stream()
                .flatMap(Collection::stream)
                .forEach(helper -> helper.render(pose, alpha));
    }

    private static void renderActiveMiniaturizationFields(Minecraft mc) {
        final var nodeStorage = mc.gameRenderer.getSubmitNodeStorage();
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
        event.registerPipeline(CCRenderPipelines.FIELD_OUTLINE_PIPELINE);
        event.registerPipeline(CCRenderPipelines.FIELD_PIPELINE);
    }
}
