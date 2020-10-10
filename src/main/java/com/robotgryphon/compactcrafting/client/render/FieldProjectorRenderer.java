package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.Constants;
import com.robotgryphon.compactcrafting.field.FieldProjection;
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
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

import java.awt.*;
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

    private IBakedModel bakedModelCached;

    private Color colorProjectionArc = new Color(255, 106, 0, 200);
    private final Color colorProjectionCube = new Color(255, 106, 0, 100);
    private final Color colorScanLine = new Color(255, 106, 0, 200);

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffers, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn);

        Optional<FieldProjection> fieldProjection = tile.getField();
        if (fieldProjection.isPresent()) {
            FieldProjection fp = fieldProjection.get();
            BlockPos center = fp.getCenterPosition();
            int fieldSize = fp.getFieldSize().getSize();

            AxisAlignedBB cube = fp.getBounds();

            renderFaces(tile, matrixStack, buffers, cube, 0);

            // TODO - WIP ARC CODE
            // drawProjectorArcs(tile, matrixStack, buffers, cube, fieldSize);

            if (tile.isMainProjector()) {
                drawScanLines(tile, matrixStack, buffers, cube, fieldSize);
                renderProjectionCube(tile, matrixStack, buffers, cube, fieldSize);
            }
        }
    }

    private IBakedModel getModel() {
        if (bakedModelCached == null) {
            ModelManager models = Minecraft.getInstance()
                    .getItemRenderer()
                    .getItemModelMesher()
                    .getModelManager();


            bakedModelCached = models.getModel(Constants.FIELD_DISH_RL);
        }

        return bakedModelCached;
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, Vector3f position) {
        renderer.pos(stack.getLast().getMatrix(), position.getX(), position.getY(), position.getZ())
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .lightmap(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void drawRing(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB bounds, Color color) {
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
    private void drawCube(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, Color color) {
        drawCubeFace(builder, mx, cube, color, Direction.NORTH);
        drawCubeFace(builder, mx, cube, color, Direction.SOUTH);
        drawCubeFace(builder, mx, cube, color, Direction.WEST);
        drawCubeFace(builder, mx, cube, color, Direction.EAST);
        drawCubeFace(builder, mx, cube, color, Direction.UP);
        drawCubeFace(builder, mx, cube, color, Direction.DOWN);
    }

    private void drawCubeFace(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, Color color, Direction face) {
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

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

        IVertexBuilder cutoutBlocks = buffer.getBuffer(Atlases.getCutoutBlockType());
        IModelData model = ModelDataManager.getModelData(te.getWorld(), te.getPos());

        IBakedModel baked = this.getModel();

        mx.push();

        mx.translate(.5, 0, .5);

        RotationSpeed speed = RotationSpeed.SLOW;
        double yaw = Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / speed.getSpeed()) * 10;
        // double yaw = Math.random();

        Direction facing = state.get(FieldProjectorBlock.FACING);
        float angle = facing.getHorizontalAngle() - 90;
        mx.rotate(Vector3f.YN.rotationDegrees(angle));

        float yDiskOffset = -0.66f;
        mx.translate(0.0, -yDiskOffset, 0.0);
        mx.rotate(Vector3f.ZP.rotationDegrees((float) yaw));
        mx.translate(0.0, yDiskOffset, 0.0);

        mx.translate(-.5, 0, -.5);

        blockRenderer.getBlockModelRenderer()
                .renderModel(mx.getLast(), cutoutBlocks, state,
                        baked, 1, 1, 1,
                        combinedLight, combinedOverlay, model);

        mx.pop();
    }

    /**
     * TODO - Main projector (single, NORTH projector)
     * TODO - render scan line animated over main crafting projection
     * TODO - render main crafting projection cube
     * <p>
     * TODO - All projectors (individually)
     * TODO - render projection arc connecting projector to large cube
     */

    private void translateRendererToCube(FieldProjectorTile tile, MatrixStack mx, AxisAlignedBB cube, int cubeSize) {
        BlockPos center = tile.getField().get().getCenterPosition();

        // Center on projector
        mx.translate(-cube.minX, -cube.minY, -cube.minZ);
        mx.translate(-cubeSize, -cubeSize, -cubeSize);

        // Now move to actual center
        BlockPos projectorPos = tile.getPos();
        BlockPos offsetToCenter = center.subtract(projectorPos);

        mx.translate(offsetToCenter.getX(), offsetToCenter.getY(), offsetToCenter.getZ());
    }

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void renderProjectionCube(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {
        mx.push();

        translateRendererToCube(tile, mx, cube, cubeSize);

        IVertexBuilder builder = buffers.getBuffer(RenderTypesExtensions.PROJECTION_FIELD_RENDERTYPE);

        double expansion = 0.005;
        AxisAlignedBB slightlyBiggerBecauseFoxes = cube
                .expand(expansion, expansion, expansion)
                .expand(-expansion, -expansion, -expansion);

        drawCube(builder, mx, slightlyBiggerBecauseFoxes, colorProjectionCube);

        mx.pop();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {

        IVertexBuilder builder = buffers.getBuffer(RenderTypesExtensions.getLines());

        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.getYSize());
        double scanHeight = (cube.minY + zAngle);

        Vector3d centerPos = cube.getCenter();

        Direction facing = tile.getBlockState().get(FieldProjectorBlock.FACING);
        Quaternion rotation = facing.getRotation();
        Vector3i identity = facing.getDirectionVec();

        mx.push();

        mx.translate(.5, .5, .5);

        mx.rotate(rotation);

        // 0, 0, 0 is now the edge of the projector's space
        addColoredVertex(builder, mx, colorProjectionArc, new Vector3f(0f, 0f, 0f));

        // Now translate to center of projection field
        addColoredVertex(builder, mx, colorProjectionArc, new Vector3f((float) -scanHeight, 3, 0));

        mx.pop();

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
    private void drawScanLines(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {
        IVertexBuilder builder = buffers.getBuffer(RenderType.getLines());

        mx.push();

        translateRendererToCube(tile, mx, cube, cubeSize);

        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.getYSize());
        double scanHeight = (cube.minY + zAngle);

        AxisAlignedBB scanLineMain = new AxisAlignedBB(cube.minX, scanHeight, cube.minZ, cube.maxX, scanHeight, cube.maxZ);

        drawRing(builder, mx, scanLineMain, colorScanLine);

        mx.pop();
    }

    /**
     * @param mx
     * @param buffer
     * @param cube
     * @param extraLength
     */
    private void renderFaces(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffer, AxisAlignedBB cube, double extraLength) {

        BlockState state = tile.getBlockState();
        Direction facing = state.get(FieldProjectorBlock.FACING);

        try {
            IVertexBuilder builder = buffer.getBuffer(RenderTypesExtensions.PROJECTION_FIELD_RENDERTYPE);

            Color fieldColor = new Color(0x88FF6A00, true);

            // Draw the faces
            Vector3d start = new Vector3d(cube.minX, cube.minY, cube.minZ);
            Vector3d end = new Vector3d(cube.maxX, cube.maxY, cube.maxZ);

            Vector3f bl = new Vector3f(0, 0, 0);
            Vector3f tr = new Vector3f(1, 1, 0);

//            mx.push();

//            float angle = facing.getHorizontalAngle() - 90;
//            mx.rotate(Vector3f.YN.rotationDegrees(angle));
//
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(bl.getX(), bl.getY(), bl.getZ()));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(tr.getX(), bl.getY(), tr.getZ()));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(tr.getX(), tr.getY(), tr.getZ()));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(bl.getX(), tr.getY(), bl.getZ()));
//
//
//
//            mx.pop();


//            addColoredVertex(builder, mx, fieldColor, new Vector3f(0, 1, .5f));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(1, 1, .5f));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(1, 0, .5f));
//            addColoredVertex(builder, mx, fieldColor, new Vector3f(0, 0, .5f));

//            lines.pos(x2, y2, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//
//            lines.pos(x1, y1, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y2, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();w
//            lines.pos(x2, y2, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//
//            lines.pos(x1, y1, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x1, y2, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y2, z1).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y2, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
//            lines.pos(x2, y1, z2).color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), fieldColor.getAlpha()).endVertex();
        } catch (Exception ex) {
        }

        // Render projection planes
//        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.maxY - cube.minY);
//        double y3 = y1 + zAngle;
//        float cA2 = 0.105f;

        // Ensure both sides of the plane are visible
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

//        GlStateManager.enableCull();
    }

    @Override
    public boolean isGlobalRenderer(FieldProjectorTile te) {
        return true;
    }
}
