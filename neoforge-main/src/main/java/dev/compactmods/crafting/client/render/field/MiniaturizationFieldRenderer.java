package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.ClientUtilities;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.CubeRenderHelper;
import dev.compactmods.crafting.client.render.EnumCubeFaceCorner;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.projector.EnumProjectorColorType;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.Objects;

public class MiniaturizationFieldRenderer {

    public static void onRenderStage(RenderLevelStageEvent.AfterTranslucentBlocks evt) {
        final var mc = Minecraft.getInstance();
        final var level = mc.level;

        if (level == null) return;

        final var partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
            fields.getFields().forEach(field -> {
                render(level, field, partialTicks, evt.getPoseStack(), buffers);
            });
        });
    }

    public static void render(Level level, IMiniaturizationField<MiniaturizationRecipe> field, float partialTicks, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        // GhostRenderer.render(Blocks.GREEN_STAINED_GLASS.defaultBlockState(), field.getCenter(), matrixStack);
        final Minecraft mc = Minecraft.getInstance();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        Vec3 projectedView = mainCamera.position();

        pose.pushPose();
        {
            pose.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            if(field.getCraftingState() == EnumCraftingState.CRAFTING) {
//                CraftingPreviewRenderer.render(field.currentRecipe(), field.getProgress(), pose, buffers, 0, 0);
            }

            drawMainField(level, pose, buffers, field);

            field.getProjectors()
                    .locations()
                    .stream()
                    .map(level::getBlockEntity)
                    .map(be -> be instanceof FieldProjectorEntity fpe ? fpe : null)
                    .filter(Objects::nonNull)
                    .forEach(fieldProjectorEntity -> {
                        drawScanLine(fieldProjectorEntity.getProjectorSide(), pose, buffers, field.getBounds(), level.getGameTime());
                        drawProjectorArcs(fieldProjectorEntity, pose, buffers, field.getBounds(), level.getGameTime());
                    });

            buffers.endBatch(CCRenderTypes.FIELD_RENDER_TYPE);
            buffers.endBatch(RenderTypes.lines());
        }
        pose.popPose();
    }

    public static int getProjectionColor(EnumProjectorColorType type) {
        int base = ClientConfig.projectorColor;
        int red = ARGB.red(base);
        int green = ARGB.green(base);
        int blue = ARGB.blue(base);

        return switch (type) {
            case FIELD, SCAN_LINE -> ARGB.color(50, red, green, blue);
            case PROJECTOR_FACE -> ARGB.color(250, red, green, blue);
        };
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private static void drawScanLine(Direction side, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {
        VertexConsumer builder = buffers.getBuffer(RenderTypes.lines());

        mx.pushPose();

        int colorScanLine = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        Vec3 left = CubeRenderHelper.getScanLineLeft(side, fieldBounds, gameTime);
        Vec3 right = CubeRenderHelper.getScanLineRight(side, fieldBounds, gameTime);

        CubeRenderHelper.drawLine(builder, mx, colorScanLine, left, right);
        mx.popPose();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private static void drawProjectorArcs(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource.BufferSource buffers, AABB fieldBounds, double gameTime) {

        try {

            Direction facing = tile.getProjectorSide();

            mx.pushPose();

            int colorProjectionArc = getProjectionColor(EnumProjectorColorType.FIELD);

            Vec3 scanLeft = CubeRenderHelper.getScanLineRight(facing, fieldBounds, gameTime);
            Vec3 scanRight = CubeRenderHelper.getScanLineLeft(facing, fieldBounds, gameTime);

            Vec3 projectorCenter = Vec3.atCenterOf(tile.getBlockPos());

            // 0, 0, 0 is now the edge of the projector's space
            final Matrix4f p = mx.last().pose();
            final var n = mx.last();

            VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

            builder.addVertex(p, (float) projectorCenter.x, (float) projectorCenter.y + 0.2f, (float) projectorCenter.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, (float) scanLeft.x, (float) scanLeft.y, (float) scanLeft.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, (float) scanRight.x, (float) scanRight.y, (float) scanRight.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, (float) projectorCenter.x, (float) projectorCenter.y + 0.2f, (float) projectorCenter.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            mx.popPose();
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error(ex);
        }
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private static void drawMainField(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers, IMiniaturizationField<MiniaturizationRecipe> field) {

        final var fieldBounds = field.getBounds();
        final var fieldProjectors = field.getProjectors().locations();

        HitResult hr = Minecraft.getInstance().hitResult;
        if (hr instanceof BlockHitResult bhr && ClientUtilities.isDebugScreenOpen() && fieldProjectors.contains(bhr.getBlockPos())) {
            doDebugFieldRender(level, mx, buffers, fieldBounds, bhr);
        }

        VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion);

        // Each projector renders its face
        // North and South projectors render the top and bottom faces
        int color = getProjectionColor(EnumProjectorColorType.FIELD);

        for (var dir : Direction.values()) {
            CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, dir);
        }

        buffers.endBatch(CCRenderTypes.FIELD_RENDER_TYPE);
    }

    private static void doDebugFieldRender(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers, AABB fieldBounds, BlockHitResult bhr) {
        var state = level.getBlockState(bhr.getBlockPos());
        if (!state.is(CCBlocks.FIELD_PROJECTOR_BLOCK.get()))
            return;

        final var hoveringProjector = FieldProjectorBlock.getDirection(level, bhr.getBlockPos())
                .orElse(Direction.DOWN)
                .getOpposite();

        VertexConsumer lineBuilder = buffers.getBuffer(RenderTypes.lines());

        Vec3 hoveredProjectorPos = Vec3.atCenterOf(bhr.getBlockPos());
        Vec3 debugOrigin = new Vec3(.5, .5, .5)
                .add(hoveredProjectorPos);

        Vec3 bottomLeft = CubeRenderHelper
                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.BOTTOM_LEFT);

        Vec3 bottomRight = CubeRenderHelper
                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.BOTTOM_RIGHT);

        Vec3 topLeft = CubeRenderHelper
                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.TOP_LEFT);

        Vec3 topRight = CubeRenderHelper
                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.TOP_RIGHT);

        mx.pushPose();
        {
            CubeRenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, bottomLeft);
            CubeRenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, bottomRight);
            CubeRenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, topRight);
            CubeRenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, topLeft);
        }
        mx.popPose();

        buffers.endBatch(RenderTypes.lines());
    }
}
