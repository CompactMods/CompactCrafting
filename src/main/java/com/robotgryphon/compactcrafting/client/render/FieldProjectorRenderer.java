package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

import java.awt.*;

import static net.minecraft.client.renderer.RenderType.makeType;

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

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        AxisAlignedBB cube = new AxisAlignedBB(tileEntityIn.getPos()).grow(3);
        renderFaces(tileEntityIn, matrixStackIn, bufferIn, cube, 0);
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
     *
     * TODO - All projectors (individually)
     * TODO - render projection arc connecting projector to large cube
     */

    /**
     * Handles rendering the main projection cube in the center of the projection area.
     * Should only be called by the main projector (typically the NORTH projector)
     */
    private void renderProjectionCube() {
        // Draw the faces
//        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//
//        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//
//        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private void drawProjectorArcs() {
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
    private void drawScanLines() {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.getBuffer();
//        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//
//        int color = 0xFF6A00;
//        float cR = (color >> 16 & 255) / 255.0f;
//        float cG = (color >> 8 & 255) / 255.0f;
//        float cB = (color & 255) / 255.0f;
//        float cA = 0.5f;
//
//        double x1 = cube.minX;
//        double x2 = cube.maxX;
//        double y1 = cube.minY;
//        double y2 = cube.maxY;
//        double z1 = cube.minZ;
//        double z2 = cube.maxZ;
//
//        // Draw the up and down bouncing lines on the sides
//        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (y2 - y1);
//        double y3 = y1 + zAngle;
//        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA).endVertex();
//
//        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA).endVertex();
//
//        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA).endVertex();
//
//        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA).endVertex();
//        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA).endVertex();
//
//        tessellator.draw();
    }

    /**
     *
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

            int color = 0x88FF6A00;
            Color fieldColor = new Color(color, true);

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
//            lines.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();w
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
