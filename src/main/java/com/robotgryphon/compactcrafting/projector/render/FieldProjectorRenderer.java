package com.robotgryphon.compactcrafting.projector.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.ClientConfig;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.projector.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.LazyOptional;

public class FieldProjectorRenderer extends TileEntityRenderer<FieldProjectorTile> {

    enum RotationSpeed {
        SLOW(5000),
        MEDIUM(2500),
        FAST(1000);

        private int speed;

        RotationSpeed(int speed) {
            this.speed = speed;
        }

        public int getSpeed() {
            return speed;
        }
    }

    public static final ResourceLocation FIELD_DISH_RL = new ResourceLocation(CompactCrafting.MOD_ID, "block/field_projector_dish");

    private IBakedModel bakedModelCached;

    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffers, int combinedLightIn, int combinedOverlayIn) {

        if (!tile.getBlockState().getValue(FieldProjectorBlock.ACTIVE))
            return;

        long gameTime = tile.getLevel().getGameTime();

        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn, gameTime);

        field = tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);

        field.ifPresent(fp -> {
            float scale = getCraftingScale(tile.getLevel(), fp, gameTime);

            matrixStack.pushPose();

            matrixStack.scale(scale, scale, scale);

            drawScanLine(tile, fp, matrixStack, buffers, gameTime);
            drawProjectorArcs(tile, matrixStack, buffers, fp.getBounds(), fp.getFieldSize().getSize(), gameTime);
            drawFieldFace(tile, fp, matrixStack, buffers);

            matrixStack.popPose();
        });
    }

    private IBakedModel getModel() {
        if (bakedModelCached == null) {
            ModelManager models = Minecraft.getInstance()
                    .getItemRenderer()
                    .getItemModelShaper()
                    .getModelManager();


            bakedModelCached = models.getModel(FIELD_DISH_RL);
        }

        return bakedModelCached;
    }

    private float getCraftingScale(IWorldReader level, IMiniaturizationField field, double gameTime) {
        EnumCraftingState state = field.getCraftingState();
        if (state == EnumCraftingState.CRAFTING) {
            FieldCraftingPreviewTile preview = (FieldCraftingPreviewTile) level
                    .getBlockEntity(field.getCenterPosition());

            // No preview tile found, not actually crafting rn
            if (preview == null)
                return 1;

            double craftProgress = preview.getProgress();

            double recipeProgress = (double) field.getCurrentRecipe().map(MiniaturizationRecipe::getTicks).orElse(100);

            double progress = 1.0d - (craftProgress / recipeProgress);
            return (float) (progress * (1.0f - ((Math.sin(Math.toDegrees(gameTime) / 2000) + 1.0f) * 0.1f)));
        }

        return 1;
    }

    private void renderDish(FieldProjectorTile te, MatrixStack mx, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, long gameTime) {

        BlockState state = te.getBlockState();

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        IVertexBuilder cutoutBlocks = buffer.getBuffer(Atlases.cutoutBlockSheet());
        IModelData model = ModelDataManager.getModelData(te.getLevel(), te.getBlockPos());

        IBakedModel baked = this.getModel();

        mx.pushPose();

        mx.translate(.5, 0, .5);

        double yaw = Math.sin(Math.toDegrees(gameTime) / RotationSpeed.MEDIUM.getSpeed()) * 10;
        // double yaw = Math.random();

        Direction facing = state.getValue(FieldProjectorBlock.FACING);
        float angle = facing.toYRot() - 90;
        mx.mulPose(Vector3f.YN.rotationDegrees(angle));

        float yDiskOffset = -0.66f;
        mx.translate(0.0, -yDiskOffset, 0.0);
        mx.mulPose(Vector3f.ZP.rotationDegrees((float) yaw));
        mx.translate(0.0, yDiskOffset, 0.0);

        mx.translate(-.5, 0, -.5);

        int faceColor = getProjectionColor(EnumProjectorColorType.PROJECTOR_FACE);
        float red = ColorHelper.PackedColor.red(faceColor) / 255f;
        float green = ColorHelper.PackedColor.green(faceColor) / 255f;
        float blue = ColorHelper.PackedColor.blue(faceColor) / 255f;

        blockRenderer.getModelRenderer()
                .renderModel(mx.last(), cutoutBlocks, state,
                        baked,
                        red,
                        green,
                        blue,
                        combinedLight, combinedOverlay, model);

        mx.popPose();
    }

    private void translateRendererToCube(FieldProjectorTile tile, MatrixStack mx, IMiniaturizationField field) {
        BlockPos center = field.getCenterPosition();

        AxisAlignedBB cube = field.getBounds();
        int fieldDim = field.getFieldSize().getSize();

        // Center on projector
        mx.translate(-cube.minX, -cube.minY, -cube.minZ);
        mx.translate(-fieldDim, -fieldDim, -fieldDim);

        // Now move to actual center
        BlockPos projectorPos = tile.getBlockPos();
        BlockPos offsetToCenter = center.subtract(projectorPos);

        mx.translate(offsetToCenter.getX(), offsetToCenter.getY(), offsetToCenter.getZ());
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void drawFieldFace(FieldProjectorTile tile, IMiniaturizationField field, MatrixStack mx, IRenderTypeBuffer buffers) {

        IVertexBuilder lineBuilder = buffers.getBuffer(RenderType.lines());
        Direction projectorDir = tile.getProjectorSide();

        Vector3d tilePos = new Vector3d(
                tile.getBlockPos().getX(),
                tile.getBlockPos().getY(),
                tile.getBlockPos().getZ()
        );

        Vector3d debugOrigin = new Vector3d(.5, .5, .5);

        Vector3d bottomLeft = CubeRenderHelper
                .getCubeFacePoint(field.getBounds(), projectorDir, EnumCubeFaceCorner.BOTTOM_LEFT)
                .subtract(tilePos);

        Vector3d bottomRight = CubeRenderHelper
                .getCubeFacePoint(field.getBounds(), projectorDir, EnumCubeFaceCorner.BOTTOM_RIGHT)
                .subtract(tilePos);

        Vector3d topLeft = CubeRenderHelper
                .getCubeFacePoint(field.getBounds(), projectorDir, EnumCubeFaceCorner.TOP_LEFT)
                .subtract(tilePos);

        Vector3d topRight = CubeRenderHelper
                .getCubeFacePoint(field.getBounds(), projectorDir, EnumCubeFaceCorner.TOP_RIGHT)
                .subtract(tilePos);
        mx.popPose();

        mx.pushPose();
        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF000000, debugOrigin);
        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFF0000, bottomLeft);

        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF000000, debugOrigin);
        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF00FF00, bottomRight);

        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF000000, debugOrigin);
        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF0000FF, topRight);

        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFF000000, debugOrigin);
        CubeRenderHelper.addColoredVertex(lineBuilder, mx, 0xFFFFFFFF, topLeft);
        mx.popPose();

        mx.pushPose();

        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        double expansion = 0.005;
        AxisAlignedBB slightlyBiggerBecauseFoxes = field.getBounds()
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion)
                .move(tilePos.reverse());

        // Each projector renders its face
        // North and South projectors render the top and bottom faces
        int color = getProjectionColor(EnumProjectorColorType.FIELD);
        switch (projectorDir) {
            case NORTH:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, getProjectionColor(EnumProjectorColorType.FIELD), Direction.UP);

            case SOUTH:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, getProjectionColor(EnumProjectorColorType.FIELD), Direction.DOWN);

            default:
                CubeRenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, color, projectorDir);
                break;
        }
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int fieldSize, double gameTime) {

        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        Direction facing = tile.getBlockState().getValue(FieldProjectorBlock.FACING);

        Vector3d tilePos = new Vector3d(
                tile.getBlockPos().getX() + 0.5d,
                tile.getBlockPos().getY() + 0.5d,
                tile.getBlockPos().getZ() + 0.5d
        );

        mx.pushPose();

        mx.translate(.5, .5, .5);

        // mx.mulPose(rotation);

        int colorProjectionArc = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        Vector3d scanLeft = CubeRenderHelper.getScanLineRight(facing.getOpposite(), cube, gameTime).subtract(tilePos);
        Vector3d scanRight = CubeRenderHelper.getScanLineLeft(facing.getOpposite(), cube, gameTime).subtract(tilePos);

        // 0, 0, 0 is now the edge of the projector's space
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, Vector3d.ZERO);
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, scanLeft);
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, scanRight);
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, Vector3d.ZERO);

        mx.popPose();
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private void drawScanLine(FieldProjectorTile tile, IMiniaturizationField field, MatrixStack mx, IRenderTypeBuffer buffers, double gameTime) {
        IVertexBuilder builder = buffers.getBuffer(RenderType.lines());

        Vector3d tilePos = new Vector3d(
                tile.getBlockPos().getX() + 0.5d,
                tile.getBlockPos().getY() + 0.5d,
                tile.getBlockPos().getZ() + 0.5d
        );

        mx.pushPose();
        mx.translate(.5, .5, .5);

        int colorScanLine = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        Direction face = tile.getProjectorSide();
        Vector3d left = CubeRenderHelper.getScanLineLeft(face, field.getBounds(), gameTime).subtract(tilePos);
        Vector3d right = CubeRenderHelper.getScanLineRight(face, field.getBounds(), gameTime).subtract(tilePos);

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
        int red = ColorHelper.PackedColor.red(base);
        int green = ColorHelper.PackedColor.green(base);
        int blue = ColorHelper.PackedColor.blue(base);

        switch (type) {
            case FIELD:
            case SCAN_LINE:
                return ColorHelper.PackedColor.color(100, red, green, blue);

            case PROJECTOR_FACE:
                return ColorHelper.PackedColor.color(250, red, green, blue);
        }

        return 0x00FFFFFF;
    }
}
