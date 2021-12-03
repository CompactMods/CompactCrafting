package dev.compactmods.crafting.projector.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.CubeRenderHelper;
import dev.compactmods.crafting.client.render.EnumCubeFaceCorner;
import dev.compactmods.crafting.client.render.RotationSpeed;
import dev.compactmods.crafting.projector.EnumProjectorColorType;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorTile;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;

public class FieldProjectorRenderer implements BlockEntityRenderer<FieldProjectorTile> {

    public static final ResourceLocation FIELD_DISH_RL = new ResourceLocation(CompactCrafting.MOD_ID, "block/field_projector_dish");

    private BakedModel bakedModelCached;

    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();

    public FieldProjectorRenderer(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(FieldProjectorTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        long gameTime = tile.getLevel().getGameTime();

        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn, gameTime);

        AABB bounds = tile.getField().map(field -> {
            double scale = MathUtil.calculateFieldScale(field);
            return field.getBounds().deflate((1 - scale) * (field.getFieldSize().getSize() / 2.0));
        }).orElseGet(() -> {
            BlockState state = tile.getBlockState();
            final MiniaturizationFieldSize fieldSize = state.getValue(FieldProjectorBlock.SIZE);
            final BlockPos center = fieldSize.getCenterFromProjector(tile.getBlockPos(), state.getValue(FieldProjectorBlock.FACING));
            return fieldSize.getBoundsAtPosition(center);
        });

        matrixStack.pushPose();

        drawScanLine(tile, matrixStack, buffers, bounds, gameTime);
        drawFieldFace(tile, matrixStack, buffers, bounds);
        drawProjectorArcs(tile, matrixStack, buffers, bounds, gameTime);

        matrixStack.popPose();
    }

    private BakedModel getModel() {
        if (bakedModelCached == null) {
            ModelManager models = Minecraft.getInstance()
                    .getBlockRenderer()
                    .getBlockModelShaper()
                    .getModelManager();

            bakedModelCached = models.getModel(FIELD_DISH_RL);
        }

        return bakedModelCached;
    }

    private void renderDish(FieldProjectorTile te, PoseStack mx, MultiBufferSource buffer, int combinedLight, int combinedOverlay, long gameTime) {

        BlockState state = te.getBlockState();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        VertexConsumer cutoutBlocks = buffer.getBuffer(Sheets.cutoutBlockSheet());
        // IModelData model = ModelDataManager.getModelData(te.getLevel(), te.getBlockPos());

        BakedModel baked = this.getModel();

        mx.pushPose();

        mx.translate(.5, 0, .5);

        double yaw = Math.sin(Math.toDegrees(gameTime) / RotationSpeed.MEDIUM.getSpeed()) * 10;
        // double yaw = Math.random();

        Direction facing = state.getValue(FieldProjectorBlock.FACING);
        if (facing != Direction.WEST) {
            float angle = facing.toYRot() - 90;
            mx.mulPose(Vector3f.YN.rotationDegrees(angle));
        }

        float yDiskOffset = -0.66f;
        mx.translate(0.0, -yDiskOffset, 0.0);
        mx.mulPose(Vector3f.ZP.rotationDegrees((float) yaw));
        mx.translate(0.0, yDiskOffset, 0.0);

        mx.translate(-.5, 0, -.5);

        int faceColor = getProjectionColor(EnumProjectorColorType.PROJECTOR_FACE);
        float red = FastColor.ARGB32.red(faceColor) / 255f;
        float green = FastColor.ARGB32.green(faceColor) / 255f;
        float blue = FastColor.ARGB32.blue(faceColor) / 255f;

        blockRenderer.getModelRenderer()
                .renderModel(mx.last(), cutoutBlocks, state,
                        baked,
                        red,
                        green,
                        blue,
                        combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        mx.popPose();
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void drawFieldFace(FieldProjectorTile tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds) {

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

        if (hoveringProjector) {
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

        VertexConsumer builder = buffers.getBuffer(RenderType.lightning());

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion)
                .move(tilePos.reverse());

        // Each projector renders its face
        // North and South projectors render the top and bottom faces
        int color = getProjectionColor(EnumProjectorColorType.FIELD);

        if(projectorDir == Direction.NORTH) {
            for(Direction dir : Direction.values())
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, dir);
        }

//        switch (projectorDir) {
//            case NORTH:
//                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, Direction.UP);
//                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
//                break;
//
//            case SOUTH:
//                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, Direction.DOWN);
//                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
//                break;
//
//            default:
//                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
//                break;
//        }
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {

        try {
            VertexConsumer builder = buffers.getBuffer(RenderType.lightning());

            Direction facing = tile.getProjectorSide();

            Vec3 tilePos = new Vec3(
                    tile.getBlockPos().getX() + 0.5d,
                    tile.getBlockPos().getY() + 0.5d,
                    tile.getBlockPos().getZ() + 0.5d
            );

            mx.pushPose();

            mx.translate(.5, .5, .5);

            // mx.mulPose(rotation);

            int colorProjectionArc = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

            Vec3 scanLeft = CubeRenderHelper.getScanLineRight(facing, fieldBounds, gameTime).subtract(tilePos);
            Vec3 scanRight = CubeRenderHelper.getScanLineLeft(facing, fieldBounds, gameTime).subtract(tilePos);

            // 0, 0, 0 is now the edge of the projector's space
            final Matrix4f p = mx.last().pose();
            final Matrix3f n = mx.last().normal();

            builder.vertex(p, 0, 0.2f, 0)
                    .color(colorProjectionArc)
                    .normal(n, 0, 0, 0)
                    .endVertex();

            builder.vertex(p, (float) scanLeft.x, (float) scanLeft.y, (float) scanLeft.z)
                    .color(colorProjectionArc)
                    .normal(n, 0, 0, 0)
                    .endVertex();

            builder.vertex(p, (float) scanRight.x, (float) scanRight.y, (float) scanRight.z)
                    .color(colorProjectionArc)
                    .normal(n, 0, 0, 0)
                    .endVertex();

            builder.vertex(p, 0, 0.2f, 0)
                    .color(colorProjectionArc)
                    .normal(n, 0, 0, 0)
                    .endVertex();
            ;

            mx.popPose();
        }

        catch(Exception ex) {
            CompactCrafting.LOGGER.error(ex);
        }
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private void drawScanLine(FieldProjectorTile tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {
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

    @Override
    public boolean shouldRenderOffScreen(FieldProjectorTile te) {
        return true;
    }

    public int getProjectionColor(EnumProjectorColorType type) {
        int base = ClientConfig.projectorColor;
        // Color base = Color.red.brighter();
        int red = FastColor.ARGB32.red(base);
        int green = FastColor.ARGB32.green(base);
        int blue = FastColor.ARGB32.blue(base);

        switch (type) {
            case FIELD:
            case SCAN_LINE:
                return FastColor.ARGB32.color(50, red, green, blue);

            case PROJECTOR_FACE:
                return FastColor.ARGB32.color(250, red, green, blue);
        }

        return 0x00FFFFFF;
    }
}
