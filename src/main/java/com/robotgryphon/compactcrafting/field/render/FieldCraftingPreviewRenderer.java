package com.robotgryphon.compactcrafting.field.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Optional;

public class FieldCraftingPreviewRenderer extends TileEntityRenderer<FieldCraftingPreviewTile> {

    public FieldCraftingPreviewRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldCraftingPreviewTile tile, float partialTicks, MatrixStack mx, IRenderTypeBuffer buffers, int light, int overlay) {

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        MiniaturizationRecipe recipe = tile.getRecipe();

        if(recipe == null)
            return;

        mx.pushPose();

        mx.translate(0.5, 0.5, 0.5);

        try {
            // progress, ticks required
            double craftProgress = tile.getProgress();

            double progress = 1.0d - (craftProgress / (double) recipe.getTicks());
            long gameTime = tile.getLevel().getGameTime();

            double scale = progress * (1.0f - ((Math.sin(Math.toDegrees(gameTime) / 2000) + 1.0f) * 0.1f));

            mx.scale((float) scale, (float) scale, (float) scale);

            double angle = gameTime * (45.0f / 64.0f);
            mx.mulPose(Vector3f.YP.rotationDegrees((float) angle));

            AxisAlignedBB dimensions = recipe.getDimensions();
            mx.translate(-(dimensions.getXsize() / 2), -(dimensions.getYsize() / 2), -(dimensions.getZsize() / 2));


            double ySize = recipe.getDimensions().getYsize();

            for (int y = 0; y < ySize; y++) {
                mx.pushPose();
                mx.translate(0, y, 0);

                Optional<IRecipeLayer> layer = recipe.getLayer(y);
                int finalY = y;
                layer.ifPresent(l -> {
                    AxisAlignedBB layerBounds = BlockSpaceUtil.getLayerBoundsByYOffset(recipe.getDimensions(), finalY);
                    BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
                        mx.pushPose();
                        mx.translate(filledPos.getX(), 0, filledPos.getZ());

                        BlockPos zeroedPos = filledPos.below(finalY);
                        l.getComponentForPosition(zeroedPos)
                                .flatMap(recipe::getRecipeBlockComponent)
                                .ifPresent(comp -> {
                                    // TODO - Render switching
                                    BlockState state1 = comp.getRenderState();
                                    blockRenderer.renderBlock(state1, mx, buffers, light, overlay, EmptyModelData.INSTANCE);
                                });

                        mx.popPose();
                    });
                });

                mx.popPose();
            }
        }

        catch(Exception ex) {
            ex.printStackTrace();
        }

        mx.popPose();
    }
}
