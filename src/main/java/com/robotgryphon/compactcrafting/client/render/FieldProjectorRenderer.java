package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.Constants;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLXEXTStereoTree;

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

    // private static IModel model;
    private static IBakedModel bakedModel;
    private static final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "block/field_projector_dish");

    public FieldProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldProjectorTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        renderDish(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    private void renderDish(FieldProjectorTile te, MatrixStack mx, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        // GlStateManager.pushAttrib();
//        TextureAtlasSprite sprite = Minecraft.getInstance()
//                .getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
//                .apply(TEXTURE);



        mx.push();

        mx.translate(.5, 0, .5);

        RotationSpeed speed = RotationSpeed.SLOW;
        double yaw = Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / speed.getSpeed()) * 10;
        // double yaw = Math.random();

        float yDiskOffset = -0.66f;
        mx.translate(0.0, -yDiskOffset, 0.0);
        mx.rotate(Vector3f.ZP.rotationDegrees((float) yaw));
        mx.translate(0.0, yDiskOffset, 0.0);

        mx.translate(-.5, 0, -.5);

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        ModelManager models = Minecraft.getInstance()
                .getItemRenderer()
                .getItemModelMesher()
                .getModelManager();

        IBakedModel bakedModel = models.getModel(Constants.FIELD_DISH_RL);
        BlockState state = te.getBlockState();
        IVertexBuilder cutoutBlocks = buffer.getBuffer(Atlases.getCutoutBlockType());
        IModelData model = ModelDataManager.getModelData(te.getWorld(), te.getPos());

        blockRenderer.getBlockModelRenderer()
                .renderModel(mx.getLast(), cutoutBlocks,  state, bakedModel, 1, 1, 1, combinedLight, combinedOverlay,
                        model);

        // blockRenderer.renderBlock(state, mx, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        mx.pop();

        // GlStateManager.popAttrib();
    }
}
