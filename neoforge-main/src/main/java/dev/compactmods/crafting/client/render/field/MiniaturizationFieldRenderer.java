package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.CubeRenderHelper;
import dev.compactmods.crafting.client.render.EnumCubeFaceCorner;
import dev.compactmods.crafting.client.render.GhostRenderer;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.projector.EnumProjectorColorType;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class MiniaturizationFieldRenderer {

    public static void onRenderStage(RenderLevelStageEvent evt) {
        if(evt.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {

            final var mc = Minecraft.getInstance();
            final var level = mc.level;

            final var partialTicks = evt.getPartialTick().getGameTimeDeltaPartialTick(false);
            final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

            level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.getFields().forEach(field -> {
                    render(field, partialTicks, evt.getPoseStack());
                });
            });
        }
    }

    public static void render(IMiniaturizationField field, float partialTicks, PoseStack matrixStack) {
        GhostRenderer.render(Blocks.GREEN_CONCRETE.defaultBlockState(), field.getCenter(), matrixStack);
    }

    public static int getProjectionColor(EnumProjectorColorType type) {
        int base = ClientConfig.projectorColor;
        int red = FastColor.ARGB32.red(base);
        int green = FastColor.ARGB32.green(base);
        int blue = FastColor.ARGB32.blue(base);

        return switch (type) {
            case FIELD, SCAN_LINE -> FastColor.ARGB32.color(50, red, green, blue);
            case PROJECTOR_FACE -> FastColor.ARGB32.color(250, red, green, blue);
        };
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private static void drawScanLine(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {
        VertexConsumer builder = buffers.getBuffer(RenderType.lines());

        Vec3 tilePos = new Vec3(
                tile.getBlockPos().getX() + 0.5d,
                tile.getBlockPos().getY() + 0.5d,
                tile.getBlockPos().getZ() + 0.5d
        );

        mx.pushPose();
        mx.translate(.5, .5, .5);

        int colorScanLine = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        Direction face = tile.getProjectorSide();
        Vec3 left = CubeRenderHelper.getScanLineLeft(face, fieldBounds, gameTime).subtract(tilePos);
        Vec3 right = CubeRenderHelper.getScanLineRight(face, fieldBounds, gameTime).subtract(tilePos);

        CubeRenderHelper.addColoredVertex(builder, mx, colorScanLine, left);
        CubeRenderHelper.addColoredVertex(builder, mx, colorScanLine, right);

        mx.popPose();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {

        try {

            Direction facing = tile.getProjectorSide();

            Vec3 tilePos = new Vec3(
                    tile.getBlockPos().getX() + 0.5d,
                    tile.getBlockPos().getY() + 0.5d,
                    tile.getBlockPos().getZ() + 0.5d
            );

            mx.pushPose();

            mx.translate(.5, .5, .5);

            int colorProjectionArc = getProjectionColor(EnumProjectorColorType.FIELD);

            Vec3 scanLeft = CubeRenderHelper.getScanLineRight(facing, fieldBounds, gameTime).subtract(tilePos);
            Vec3 scanRight = CubeRenderHelper.getScanLineLeft(facing, fieldBounds, gameTime).subtract(tilePos);

            // 0, 0, 0 is now the edge of the projector's space
            final Matrix4f p = mx.last().pose();
            final var n = mx.last();

            VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

            builder.addVertex(p, 0, 0.2f, 0)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, (float) scanLeft.x, (float) scanLeft.y, (float) scanLeft.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, (float) scanRight.x, (float) scanRight.y, (float) scanRight.z)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            builder.addVertex(p, 0, 0.2f, 0)
                    .setColor(colorProjectionArc)
                    .setNormal(n, 0, 0, 0);

            mx.popPose();
        }

        catch(Exception ex) {
            CompactCrafting.LOGGER.error(ex);
        }
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void drawFieldFace(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds) {

        Direction projectorDir = tile.getProjectorSide();

        Vec3 tilePos = new Vec3(
                tile.getBlockPos().getX(),
                tile.getBlockPos().getY(),
                tile.getBlockPos().getZ()
        );

        boolean hoveringProjector = false;

        HitResult hr = Minecraft.getInstance().hitResult;
        if (hr instanceof BlockHitResult) {
            hoveringProjector = ((BlockHitResult) hr).getBlockPos().equals(tile.getBlockPos());
        }

        if (ClientConfig.doDebugRender() && hoveringProjector) {
            VertexConsumer lineBuilder = buffers.getBuffer(RenderType.lines());

            Vec3 debugOrigin = new Vec3(.5, .5, .5);

            Vec3 bottomLeft = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.BOTTOM_LEFT)
                    .subtract(tilePos);

            Vec3 bottomRight = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.BOTTOM_RIGHT)
                    .subtract(tilePos);

            Vec3 topLeft = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.TOP_LEFT)
                    .subtract(tilePos);

            Vec3 topRight = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.TOP_RIGHT)
                    .subtract(tilePos);

            mx.pushPose();
            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFF0000, debugOrigin);
            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFF0000, bottomLeft);

            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF00FF00, debugOrigin);
            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF00FF00, bottomRight);

            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF0000FF, debugOrigin);
            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF0000FF, topRight);

            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFFFFFF, debugOrigin);
            CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFFFFFF, topLeft);
            mx.popPose();
        }

        VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion)
                .move(tilePos.reverse());

        // Each projector renders its face
        // North and South projectors render the top and bottom faces
        int color = getProjectionColor(EnumProjectorColorType.FIELD);

        switch (projectorDir) {
            case NORTH:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, Direction.UP);
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
                break;

            case SOUTH:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, Direction.DOWN);
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
                break;

            default:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
                break;
        }
    }
}
