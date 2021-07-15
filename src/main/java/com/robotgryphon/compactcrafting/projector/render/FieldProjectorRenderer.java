package com.robotgryphon.compactcrafting.projector.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.ClientConfig;
import com.robotgryphon.compactcrafting.field.block.FieldCraftingPreviewBlock;
import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.projector.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import com.robotgryphon.compactcrafting.util.MathUtil;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
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
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

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
        long gameTime = tile.getLevel().getGameTime();

        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn, gameTime);

        Optional<AxisAlignedBB> fieldBounds = tile.getFieldBounds();

        fieldBounds.ifPresent(bounds -> {
            float scale = (float) getCraftingScale(tile.getLevel(), new BlockPos(bounds.getCenter()));

            bounds = bounds.deflate((1 - scale) * (bounds.getSize() / 2));

            matrixStack.pushPose();


            drawScanLine(tile, matrixStack, buffers, bounds, gameTime);
            drawProjectorArcs(tile, matrixStack, buffers, bounds, gameTime);
            drawFieldFace(tile, matrixStack, buffers, bounds);

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

    /**
     * Scaled value between 0 and 1 depending on how far along the crafting is.
     * @param level
     * @param fieldCenter
     * @return 0 is done, 1 is not yet started.
     */
    private double getCraftingScale(IWorldReader level, BlockPos fieldCenter) {
        BlockState centerState = level.getBlockState(fieldCenter);
        if (centerState.getBlock() instanceof FieldCraftingPreviewBlock) {
            FieldCraftingPreviewTile preview = (FieldCraftingPreviewTile) level.getBlockEntity(fieldCenter);

            // No preview tile found, not actually crafting rn
            if (preview == null)
                return 1;

            final IMiniaturizationRecipe recipe = preview.getRecipe();
            double progress = preview.getProgress();
            double requiredTime = recipe != null ? Math.max(1, recipe.getCraftingTime()) : 1;

            return MathUtil.calculateScale(progress, requiredTime);
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

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void drawFieldFace(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB fieldBounds) {

        Direction projectorDir = tile.getProjectorSide();

        Vector3d tilePos = new Vector3d(
                tile.getBlockPos().getX(),
                tile.getBlockPos().getY(),
                tile.getBlockPos().getZ()
        );

        boolean hoveringProjector = false;

        RayTraceResult hr = Minecraft.getInstance().hitResult;
        if (hr instanceof BlockRayTraceResult) {
            hoveringProjector = ((BlockRayTraceResult) hr).getBlockPos().equals(tile.getBlockPos());
        }

        if (ClientConfig.doDebugRender() && hoveringProjector) {
            IVertexBuilder lineBuilder = buffers.getBuffer(RenderType.lines());

            Vector3d debugOrigin = new Vector3d(.5, .5, .5);

            Vector3d bottomLeft = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.BOTTOM_LEFT)
                    .subtract(tilePos);

            Vector3d bottomRight = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.BOTTOM_RIGHT)
                    .subtract(tilePos);

            Vector3d topLeft = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.TOP_LEFT)
                    .subtract(tilePos);

            Vector3d topRight = CubeRenderHelper
                    .getCubeFacePoint(fieldBounds, projectorDir, EnumCubeFaceCorner.TOP_RIGHT)
                    .subtract(tilePos);

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
        }

        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        double expansion = 0.005;
        AxisAlignedBB slightlyBiggerBecauseFoxes = fieldBounds
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

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB fieldBounds, double gameTime) {

        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.FIELD_RENDER_TYPE);

        Direction facing = tile.getProjectorSide();

        Vector3d tilePos = new Vector3d(
                tile.getBlockPos().getX() + 0.5d,
                tile.getBlockPos().getY() + 0.5d,
                tile.getBlockPos().getZ() + 0.5d
        );

        mx.pushPose();

        mx.translate(.5, .5, .5);

        // mx.mulPose(rotation);

        int colorProjectionArc = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        Vector3d scanLeft = CubeRenderHelper.getScanLineRight(facing, fieldBounds, gameTime).subtract(tilePos);
        Vector3d scanRight = CubeRenderHelper.getScanLineLeft(facing, fieldBounds, gameTime).subtract(tilePos);

        // 0, 0, 0 is now the edge of the projector's space
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, new Vector3d(0, 0.2d, 0));
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, scanLeft);
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, scanRight);
        CubeRenderHelper.addColoredVertex(builder, mx, colorProjectionArc, new Vector3d(0, 0.2d, 0));

        mx.popPose();
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private void drawScanLine(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB fieldBounds, double gameTime) {
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
        Vector3d left = CubeRenderHelper.getScanLineLeft(face, fieldBounds, gameTime).subtract(tilePos);
        Vector3d right = CubeRenderHelper.getScanLineRight(face, fieldBounds, gameTime).subtract(tilePos);

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
                return ColorHelper.PackedColor.color(50, red, green, blue);

            case PROJECTOR_FACE:
                return ColorHelper.PackedColor.color(250, red, green, blue);
        }

        return 0x00FFFFFF;
    }
}
