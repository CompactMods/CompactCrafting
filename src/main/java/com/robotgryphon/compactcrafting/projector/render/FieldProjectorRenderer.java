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
import com.robotgryphon.compactcrafting.projector.tile.MainFieldProjectorTile;
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
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
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

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffers, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn);

        if (tile instanceof MainFieldProjectorTile) {
            MainFieldProjectorTile mainTile = (MainFieldProjectorTile) tile;
            LazyOptional<IMiniaturizationField> fieldProjection = mainTile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);
            fieldProjection.ifPresent(fp -> {
                int fieldSize = fp.getFieldSize().getSize();

                float scale = 1f;


                AxisAlignedBB cube = fp.getBounds();

                // renderFaces(tile, matrixStack, buffers, cube, 0);

                // TODO - WIP ARC CODE
                // drawProjectorArcs(tile, matrixStack, buffers, cube, fieldSize);

                if (tile.isMainProjector()) {
                    EnumCraftingState state = mainTile.getCraftingState();
                    if (state == EnumCraftingState.CRAFTING) {
                        FieldCraftingPreviewTile preview = (FieldCraftingPreviewTile) tile
                                .getLevel()
                                .getBlockEntity(fp.getCenterPosition());

                        // No preview tile found, not actually crafting rn
                        if (preview == null)
                            return;

                        double craftProgress = preview.getProgress();

                        double progress = 1.0d - (craftProgress / (double) mainTile.getCurrentRecipe().get().getTicks());

                        long gameTime = tile.getLevel().getGameTime();
                        scale = (float) (progress * (1.0f - ((Math.sin(Math.toDegrees(gameTime) / 2000) + 1.0f) * 0.1f)));
                    }

                    matrixStack.pushPose();

                    matrixStack.scale(scale, scale, scale);

                    drawScanLines(tile, fp, matrixStack, buffers, cube, fieldSize);
                    renderProjectionCube(tile, fp, matrixStack, buffers, cube, fieldSize);

                    matrixStack.popPose();
                }
            });
        }
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

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, int color, Vector3f position) {
        renderer.vertex(stack.last().pose(), position.x(), position.y(), position.z())
                .color(ColorHelper.PackedColor.red(color), ColorHelper.PackedColor.green(color), ColorHelper.PackedColor.blue(color), ColorHelper.PackedColor.alpha(color))
                .uv2(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void drawRing(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB bounds, int color) {
        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ));
        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.maxX, (float) bounds.minY, (float) bounds.minZ));

        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ));
        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.minX, (float) bounds.minY, (float) bounds.maxZ));

        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.maxX, (float) bounds.minY, (float) bounds.maxZ));
        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.maxX, (float) bounds.minY, (float) bounds.minZ));

        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.minX, (float) bounds.minY, (float) bounds.maxZ));
        addColoredVertex(builder, mx, color, new Vector3f((float) bounds.maxX, (float) bounds.minY, (float) bounds.maxZ));
    }

    /**
     * Draws a cube given a vertex builder, matrix, color, and cube bounds.
     *
     * @param builder
     * @param mx
     * @param cube
     * @param color
     */
    private void drawCube(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, int color) {
        drawCubeFace(builder, mx, cube, color, Direction.NORTH);
        drawCubeFace(builder, mx, cube, color, Direction.SOUTH);
        drawCubeFace(builder, mx, cube, color, Direction.WEST);
        drawCubeFace(builder, mx, cube, color, Direction.EAST);
        drawCubeFace(builder, mx, cube, color, Direction.UP);
        drawCubeFace(builder, mx, cube, color, Direction.DOWN);
    }

    private void drawCubeFace(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, int color, Direction face) {
        Vector3f BOTTOM_RIGHT = null,
                TOP_RIGHT = null,
                TOP_LEFT = null,
                BOTTOM_LEFT = null;

        switch (face) {
            case NORTH:
                BOTTOM_RIGHT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.minZ);
                TOP_RIGHT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.minZ);
                TOP_LEFT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.minZ);
                BOTTOM_LEFT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.minZ);
                break;

            case SOUTH:
                BOTTOM_RIGHT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.maxZ);
                TOP_RIGHT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.maxZ);
                TOP_LEFT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.maxZ);
                BOTTOM_LEFT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.maxZ);
                break;

            case WEST:
                BOTTOM_RIGHT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.maxZ);
                TOP_RIGHT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.maxZ);
                TOP_LEFT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.minZ);
                BOTTOM_LEFT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.minZ);
                break;

            case EAST:
                BOTTOM_RIGHT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.minZ);
                TOP_RIGHT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.minZ);
                TOP_LEFT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.maxZ);
                BOTTOM_LEFT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.maxZ);
                break;

            case UP:
                BOTTOM_RIGHT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.minZ);
                TOP_RIGHT = new Vector3f((float) cube.minX, (float) cube.maxY, (float) cube.maxZ);
                TOP_LEFT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.maxZ);
                BOTTOM_LEFT = new Vector3f((float) cube.maxX, (float) cube.maxY, (float) cube.minZ);
                break;

            case DOWN:
                BOTTOM_RIGHT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.maxZ);
                TOP_RIGHT = new Vector3f((float) cube.minX, (float) cube.minY, (float) cube.minZ);
                TOP_LEFT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.minZ);
                BOTTOM_LEFT = new Vector3f((float) cube.maxX, (float) cube.minY, (float) cube.maxZ);
                break;
        }

        if (BOTTOM_RIGHT == null)
            return;

        addColoredVertex(builder, mx, color, BOTTOM_RIGHT);
        addColoredVertex(builder, mx, color, TOP_RIGHT);
        addColoredVertex(builder, mx, color, TOP_LEFT);
        addColoredVertex(builder, mx, color, BOTTOM_LEFT);
    }

    private void renderDish(FieldProjectorTile te, MatrixStack mx, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        BlockState state = te.getBlockState();

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        IVertexBuilder cutoutBlocks = buffer.getBuffer(Atlases.cutoutBlockSheet());
        IModelData model = ModelDataManager.getModelData(te.getLevel(), te.getBlockPos());

        IBakedModel baked = this.getModel();

        mx.pushPose();

        mx.translate(.5, 0, .5);

        RotationSpeed speed = RotationSpeed.SLOW;

        double gameTime = te.getLevel().getGameTime();

        double yaw = Math.sin(Math.toDegrees(gameTime) / speed.getSpeed()) * 10;
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
     * TODO - Main projector (single, NORTH projector)
     * TODO - render scan line animated over main crafting projection
     * TODO - render main crafting projection cube
     * <p>
     * TODO - All projectors (individually)
     * TODO - render projection arc connecting projector to large cube
     */

    private void translateRendererToCube(FieldProjectorTile tile, IMiniaturizationField field, MatrixStack mx, AxisAlignedBB cube, int cubeSize) {
        BlockPos center = field.getCenterPosition();

        // Center on projector
        mx.translate(-cube.minX, -cube.minY, -cube.minZ);
        mx.translate(-cubeSize, -cubeSize, -cubeSize);

        // Now move to actual center
        BlockPos projectorPos = tile.getBlockPos();
        BlockPos offsetToCenter = center.subtract(projectorPos);

        mx.translate(offsetToCenter.getX(), offsetToCenter.getY(), offsetToCenter.getZ());
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void renderProjectionCube(FieldProjectorTile tile, IMiniaturizationField field, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {
        mx.pushPose();

        translateRendererToCube(tile, field, mx, cube, cubeSize);

        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.PROJECTION_FIELD_RENDERTYPE);

        double expansion = 0.005;
        AxisAlignedBB slightlyBiggerBecauseFoxes = cube
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion);

        drawCube(builder, mx, slightlyBiggerBecauseFoxes, getProjectionColor(EnumProjectorColorType.FIELD));

        mx.popPose();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {

        IVertexBuilder builder = buffers.getBuffer(RenderType.lines());

        double gameTime = tile.getLevel().getGameTime();

        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -5000) + 1.0d) / 2) * (cube.getYsize());
        double scanHeight = (cube.minY + zAngle);

        Vector3d centerPos = cube.getCenter();

        Direction facing = tile.getBlockState().getValue(FieldProjectorBlock.FACING);
        Quaternion rotation = facing.getRotation();
        Vector3i identity = facing.getNormal();

        mx.pushPose();

        mx.translate(.5, .5, .5);

        mx.mulPose(rotation);

        int colorProjectionArc = getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        // 0, 0, 0 is now the edge of the projector's space
        addColoredVertex(builder, mx, colorProjectionArc, new Vector3f(0f, 0f, 0f));

        // Now translate to center of projection field
        addColoredVertex(builder, mx, colorProjectionArc, new Vector3f((float) -scanHeight, 3, 0));

        mx.popPose();

//        mx.push();
//
//        translateRendererToCube(tile, mx, cube, cubeSize);
//
//        Vector3f LEFT_ENDPOINT = null;
//        Vector3f RIGHT_ENDPOINT = null;
//
//        switch(facing) {
//            case NORTH:
//                LEFT_ENDPOINT = new Vector3f((float) cube.minX, (float) scanHeight, (float) cube.maxZ);
//                break;
//
//            case SOUTH:
//                LEFT_ENDPOINT = new Vector3f((float) cube.maxX, (float) scanHeight, (float) cube.minZ);
//                break;
//
//            case WEST:
//                LEFT_ENDPOINT = new Vector3f((float) cube.maxX, (float) scanHeight, (float) cube.maxZ);
//                break;
//
//            case EAST:
//                LEFT_ENDPOINT = new Vector3f((float) cube.minX, (float) scanHeight, (float) cube.minZ);
//                break;
//        }
//
//        if(LEFT_ENDPOINT != null)
//            addColoredVertex(builder, mx, colorProjectionArc, LEFT_ENDPOINT);
//
//        mx.pop();

//        // Render projection planes
//        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.maxY - cube.minY);
//        double y3 = y1 + zAngle;
//        float cA2 = 0.105f;
//
//        // Ensure both sides of the plane are visible
//        GlStateManager.disableCull();
//        GL11.glDisable(GL11.GL_CULL_FACE);
//        // north -> south
//        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2-(radius-0.2f), y4, z1-(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x1+(radius-0.2f), y4, z1-(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
//
//        // east -> west
//        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2+(radius+0.8f+extraLength), y4, z2-(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2+(radius+0.8f+extraLength), y4, z1+(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
//
//        // south -> north
//        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x2-(radius-0.2f), y4, z2+(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x1+(radius-0.2f), y4, z2+(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
//
//        // west -> east
//        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x1-(radius+0.8f+extraLength), y4, z2-(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
//        buffer.pos(x1-(radius+0.8f+extraLength), y4, z1+(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
//
//
//        tessellator.draw();
//        GlStateManager.enableCull();
    }

    /**
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
     * where the projection arcs meet the main projection cube.
     */
    private void drawScanLines(FieldProjectorTile tile, IMiniaturizationField field, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {
        IVertexBuilder builder = buffers.getBuffer(RenderType.lines());


        double gameTime = Minecraft.getInstance().level.getGameTime();

        mx.pushPose();

        translateRendererToCube(tile, field, mx, cube, cubeSize);

        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -5000) + 1.0d) / 2) * (cube.getYsize());
        double scanHeight = (cube.minY + zAngle);

        AxisAlignedBB scanLineMain = new AxisAlignedBB(cube.minX, scanHeight, cube.minZ, cube.maxX, scanHeight, cube.maxZ);

        int colorScanLine = getProjectionColor(EnumProjectorColorType.SCAN_LINE);
        drawRing(builder, mx, scanLineMain, colorScanLine);

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
