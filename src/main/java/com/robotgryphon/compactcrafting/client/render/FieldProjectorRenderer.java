package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.core.Constants;
import com.robotgryphon.compactcrafting.core.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.tiles.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.tiles.MainFieldProjectorTile;
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

import java.awt.Color;
import java.util.EnumSet;

public class FieldProjectorRenderer extends TileEntityRenderer<FieldProjectorTile> {
    public enum RotationSpeed {
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

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffers, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tile, matrixStack, buffers, combinedLightIn, combinedOverlayIn);

        if (!(tile instanceof MainFieldProjectorTile))
            return;

        MainFieldProjectorTile mainTile = (MainFieldProjectorTile) tile;
        FieldProjection fp = mainTile.getField().orElse(null);
        if (fp == null)
            return;

        int fieldMagnitude = fp.getFieldSize().getMagnitude();
        AxisAlignedBB cube = fp.getBounds();

        // renderFaces(tile, matrixStack, buffers, cube, 0);

        // TODO - WIP ARC CODE
        // drawProjectorArcs(tile, matrixStack, buffers, cube, fieldMagnitude);

        Quaternion rot = null;
        float scale = fp.getFieldSize().getDimensions() + 0.005F; // Make slightly bigger to stop yucky rendering on block edges
        EnumCraftingState state = mainTile.getCraftingState();
        if (state == EnumCraftingState.CRAFTING) {
            FieldCraftingPreviewTile preview = (FieldCraftingPreviewTile) tile
                    .getLevel()
                    .getBlockEntity(fp.getCenterPosition());

            // No preview tile found, not actually crafting rn
            if (preview == null)
                return;

            double craftProgress = preview.getProgress();

            double progress = 1.0d - (craftProgress / mainTile.getCurrentRecipe().get().getTickDuration());

            scale *= Math.max(0.9f, 0.8f - (progress * (1.0f - ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 4000) + 1.0f) * 0.1f))));
            double yAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.maxY - cube.minY);
            rot = new Quaternion(0, (float) yAngle, 0, false);
        }

        matrixStack.pushPose();
        BlockPos relativeCenter = fp.getCenterPosition().subtract(mainTile.getBlockPos());
        matrixStack.translate(relativeCenter.getX() + 0.5D, relativeCenter.getY() + 0.5D, relativeCenter.getZ() + 0.5D);
        // scale *= 0.4F;
        matrixStack.scale(scale, scale, scale);
        if (rot != null)
            matrixStack.mulPose(rot);
        matrixStack.pushPose();

        AxisAlignedBB relativeCube = AxisAlignedBB.ofSize(1, 1, 1);
        drawScanLines(mainTile, matrixStack, buffers, relativeCube);
        renderProjectionCube(mainTile, matrixStack, buffers, relativeCube);

        matrixStack.popPose();
        matrixStack.popPose();
    }

    private IBakedModel getModel() {
        if (bakedModelCached == null) {
            ModelManager models = Minecraft.getInstance()
                    .getItemRenderer()
                    .getItemModelShaper()
                    .getModelManager();


            bakedModelCached = models.getModel(Constants.FIELD_DISH_RL);
        }

        return bakedModelCached;
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, Vector3d position) {
        addColoredVertex(renderer, stack, color, position.x(), position.y(), position.z());
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, double x, double y, double z) {
        addColoredVertex(renderer, stack, color, (float) x, (float) y, (float) z);
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, float x, float y, float z) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .uv2(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void drawRing(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB bounds, Color color) {
        addColoredVertex(builder, mx, color, bounds.minX, bounds.minY, bounds.minZ);
        addColoredVertex(builder, mx, color, bounds.maxX, bounds.minY, bounds.minZ);

        addColoredVertex(builder, mx, color, bounds.minX, bounds.minY, bounds.minZ);
        addColoredVertex(builder, mx, color, bounds.minX, bounds.minY, bounds.maxZ);

        addColoredVertex(builder, mx, color, bounds.maxX, bounds.minY, bounds.maxZ);
        addColoredVertex(builder, mx, color, bounds.maxX, bounds.minY, bounds.minZ);

        addColoredVertex(builder, mx, color, bounds.minX, bounds.minY, bounds.maxZ);
        addColoredVertex(builder, mx, color, bounds.maxX, bounds.minY, bounds.maxZ);
    }

    private void drawCubeFace(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, Color color, Direction face) {
        double xRight = face == Direction.SOUTH || face == Direction.EAST ? cube.maxX : cube.minX;
        double xLeft = face == Direction.SOUTH || face == Direction.WEST ? cube.minX : cube.maxX;
        double yTop = face == Direction.DOWN ? cube.minY : cube.maxY;
        double yBottom = face == Direction.UP ? cube.maxY : cube.minY;

        // bottomRight
        addColoredVertex(builder, mx, color, xRight, yBottom, face == Direction.SOUTH || face == Direction.WEST || face == Direction.DOWN ? cube.maxZ : cube.minZ);
        // topRight
        addColoredVertex(builder, mx, color, xRight, yTop, face == Direction.SOUTH || face == Direction.WEST || face == Direction.UP ? cube.maxZ : cube.minZ);
        // topLeft
        addColoredVertex(builder, mx, color, xLeft, yTop, face == Direction.SOUTH || face == Direction.EAST || face == Direction.UP ? cube.maxZ : cube.minZ);
        // bottomLeft
        addColoredVertex(builder, mx, color, xLeft, yBottom, face == Direction.SOUTH || face == Direction.EAST || face == Direction.DOWN ? cube.maxZ : cube.minZ);
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
        double yaw = Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / speed.getSpeed()) * 10;
        // double yaw = Math.random();

        Direction facing = state.getValue(FieldProjectorBlock.FACING);
        float angle = facing.toYRot() - 90;
        mx.mulPose(Vector3f.YN.rotationDegrees(angle));

        float yDiskOffset = -0.66f;
        mx.translate(0.0, -yDiskOffset, 0.0);
        mx.mulPose(Vector3f.ZP.rotationDegrees((float) yaw));
        mx.translate(0.0, yDiskOffset, 0.0);

        mx.translate(-0.5, 0, -0.5);

        Color faceColor = te.getProjectionColor(EnumProjectorColorType.PROJECTOR_FACE);
        float red = faceColor.getRed() / 255f;
        float green = faceColor.getGreen() / 255f;
        float blue = faceColor.getBlue() / 255f;

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
     * Handles rendering the main projection cube in the center of the projection area. Should only be called by the main projector (typically the NORTH projector)
     */
    private void renderProjectionCube(MainFieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube) {
        IVertexBuilder builder = buffers.getBuffer(CCRenderTypes.PROJECTION_FIELD);

        Color color = tile.getProjectionColor(EnumProjectorColorType.FIELD);
        for (Direction direction : Direction.values()) {
            drawCubeFace(builder, mx, cube, color, direction);
        }
        // This is a yucky workaround to keep consistent behavior vs. sometimes randomly flashing shit
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(CCRenderTypes.PROJECTION_FIELD);
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection in the center of the crafting area.
     */
    private void drawProjectorArcs(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffers, AxisAlignedBB cube, int cubeSize) {

        IVertexBuilder builder = buffers.getBuffer(RenderType.lines());

        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.getYsize());
        double scanHeight = (cube.minY + zAngle);

        Vector3d centerPos = cube.getCenter();

        Direction facing = tile.getBlockState().getValue(FieldProjectorBlock.FACING);
        Quaternion rotation = facing.getRotation();
        Vector3i identity = facing.getNormal();

        mx.pushPose();

        mx.translate(0.5, 0.5, 0.5);

        mx.mulPose(rotation);

        Color colorProjectionArc = tile.getProjectionColor(EnumProjectorColorType.SCAN_LINE);

        // 0, 0, 0 is now the edge of the projector's space
        addColoredVertex(builder, mx, colorProjectionArc, 0, 0, 0);

        // Now translate to center of projection field
        addColoredVertex(builder, mx, colorProjectionArc, -scanHeight, 3, 0);

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
     * Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly where the projection arcs meet the main projection cube.
     */
    private void drawScanLines(MainFieldProjectorTile tile, MatrixStack matrixStackIn, IRenderTypeBuffer buffers, AxisAlignedBB cube) {
        IVertexBuilder builder = buffers.getBuffer(RenderType.lines());

        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.getYsize());
        double scanHeight = (cube.minY + zAngle);

        AxisAlignedBB scanLineMain = new AxisAlignedBB(cube.minX, scanHeight, cube.minZ, cube.maxX, scanHeight, cube.maxZ);

        Color colorScanLine = tile.getProjectionColor(EnumProjectorColorType.SCAN_LINE);
        drawRing(builder, matrixStackIn, scanLineMain, colorScanLine);
    }

    /**
     * @param mx
     * @param buffer
     * @param cube
     * @param extraLength
     */
    private void renderFaces(FieldProjectorTile tile, MatrixStack mx, IRenderTypeBuffer buffer, AxisAlignedBB cube, double extraLength) {

        BlockState state = tile.getBlockState();
        Direction facing = state.getValue(FieldProjectorBlock.FACING);

        try {
            IVertexBuilder builder = buffer.getBuffer(CCRenderTypes.PROJECTION_FIELD);

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
    public boolean shouldRenderOffScreen(FieldProjectorTile te) {
        return true;
    }
}
