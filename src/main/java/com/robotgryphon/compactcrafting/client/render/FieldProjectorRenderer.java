package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.Constants;
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
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

import java.awt.*;

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

//    RenderType t = RenderType.makeType("my_type", DefaultVertexFormats.POSITION_COLOR, 7, 256,
//            false, true, RenderType.State.getBuilder()
//                    .transparency(TRANSLUCENT_TRANSPARENCY)
//                    .writeMask(COLOR_DEPTH_WRITE)
//                    .build(false));

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);


    }

    @Override
    public void render(FieldProjectorTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        AxisAlignedBB cube = new AxisAlignedBB(tileEntityIn.getPos()).grow(3);
        renderFaces(matrixStackIn, bufferIn, cube, 0);
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

    /**
     * Draw a coloured line from a starting vertex to an end vertex
     *
     * @param matrixPos    the current transformation matrix
     * @param renderBuffer the vertex builder used to draw the line
     * @param startVertex
     * @param endVertex
     */
    private static void drawLine(Matrix4f matrixPos, IVertexBuilder renderBuffer, Color color,
                                 Vector3d startVertex, Vector3d endVertex) {
        renderBuffer.pos(matrixPos, (float) startVertex.x, (float) startVertex.y, (float) startVertex.z)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())   // there is also a version for floats (0 -> 1)
                .endVertex();
        renderBuffer.pos(matrixPos, (float) endVertex.getX(), (float) endVertex.getY(), (float) endVertex.getZ())
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())   // there is also a version for floats (0 -> 1)
                .endVertex();
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

    private void renderFaces(MatrixStack mx, IRenderTypeBuffer buffer, AxisAlignedBB cube, double extraLength) {

        try {
            IVertexBuilder lines = buffer.getBuffer(RenderType.getLines());

            //region color and vertex positions
            int color = 0xFF6A00;
            float cR = (color >> 16 & 255) / 255.0f;
            float cG = (color >> 8 & 255) / 255.0f;
            float cB = (color & 255) / 255.0f;
            float cA = 0.15f;

            double x1 = cube.minX;
            double x2 = cube.maxX;
            double y1 = cube.minY;
            double y2 = cube.maxY;
            double z1 = cube.minZ;
            double z2 = cube.maxZ;
            double radius = (cube.maxY - cube.minY) / 2;

            double y4 = cube.maxY - radius + 0.3d;
            //endregion

            // Draw the faces
            Vector3d start = new Vector3d(cube.minX, cube.minY, cube.minZ);
            Vector3d end = new Vector3d(cube.maxX, cube.maxY, cube.maxZ);

            drawLine(mx.getLast().getMatrix(), lines, Color.orange, start, end);

//            lines.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//
//            lines.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//
//            lines.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//            lines.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
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
}
