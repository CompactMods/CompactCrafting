package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.MiniaturizationFieldRenderBuilder;
import dev.compactmods.crafting.core.CCAttachments;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class MiniaturizationFieldRenderer {

    public static void onRenderStage(RenderLevelStageEvent.AfterTranslucentBlocks evt) {
        final var mc = Minecraft.getInstance();
        final var level = mc.level;

        if (level == null) return;

        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
            fields.getFields().forEach(field -> {
                render(level, field, evt.getPoseStack(), buffers);
            });
        });
    }

    public static void render(Level level, IMiniaturizationField field, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        // GhostRenderer.render(Blocks.GREEN_STAINED_GLASS.defaultBlockState(), projectors.getCenter(), matrixStack);
        final Minecraft mc = Minecraft.getInstance();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        Vec3 projectedView = mainCamera.position();

        pose.pushPose();
        {
            pose.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            if(field.getCraftingState() == EnumCraftingState.CRAFTING) {
//                CraftingPreviewRenderer.render(projectors.currentRecipe(), projectors.getProgress(), pose, buffers, 0, 0);
            }

            final var renderInstructions = MiniaturizationFieldRenderBuilder
                    .forDefault(ClientConfig.projectorColor)
                    .withFieldBoundaries(field.getBounds())
                    .withProjectors(field.getProjectors())
                    .build();

            for(final var inst : renderInstructions) {
                inst.draw(level, pose, buffers);
            }

//            projectors.getProjectors()
//                    .locations()
//                    .stream()
//                    .map(level::getBlockEntity)
//                    .map(be -> be instanceof FieldProjectorEntity fpe ? fpe : null)
//                    .filter(Objects::nonNull)
//                    .forEach(fieldProjectorEntity -> {
//                        drawProjectorArcs(fieldProjectorEntity, pose, buffers, projectors.getBounds(), level.getGameTime());
//                    });
        }
        pose.popPose();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
//    private static void drawProjectorArcs(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource.BufferSource buffers, AABB fieldBounds, double gameTime) {
//
//        try {
//
//            Direction facing = tile.getProjectorSide();
//
//            mx.pushPose();
//
//            int colorProjectionArc = getProjectionColor(EnumProjectorColorType.FIELD);
//
//            Vec3 scanLeft = RenderHelper.getScanLineRight(facing, fieldBounds, gameTime);
//            Vec3 scanRight = RenderHelper.getScanLineLeft(facing, fieldBounds, gameTime);
//
//            Vec3 projectorCenter = Vec3.atCenterOf(tile.getBlockPos());
//
//            // 0, 0, 0 is now the edge of the projector's space
//            final Matrix4f p = mx.last().pose();
//            final var n = mx.last();
//
//            VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);
//
//            builder.addVertex(p, (float) projectorCenter.x, (float) projectorCenter.y + 0.2f, (float) projectorCenter.z)
//                    .setColor(colorProjectionArc)
//                    .setNormal(n, 0, 0, 0);
//
//            builder.addVertex(p, (float) scanLeft.x, (float) scanLeft.y, (float) scanLeft.z)
//                    .setColor(colorProjectionArc)
//                    .setNormal(n, 0, 0, 0);
//
//            builder.addVertex(p, (float) scanRight.x, (float) scanRight.y, (float) scanRight.z)
//                    .setColor(colorProjectionArc)
//                    .setNormal(n, 0, 0, 0);
//
//            builder.addVertex(p, (float) projectorCenter.x, (float) projectorCenter.y + 0.2f, (float) projectorCenter.z)
//                    .setColor(colorProjectionArc)
//                    .setNormal(n, 0, 0, 0);
//
//            mx.popPose();
//        } catch (Exception ex) {
//            CompactCraftingCommon.LOGGER.error(ex);
//        }
//    }


}
