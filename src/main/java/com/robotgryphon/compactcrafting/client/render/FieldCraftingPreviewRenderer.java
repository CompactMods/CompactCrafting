package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.blocks.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Optional;

public class FieldCraftingPreviewRenderer extends TileEntityRenderer<FieldCraftingPreviewTile> {

    public FieldCraftingPreviewRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FieldCraftingPreviewTile tile, float partialTicks, MatrixStack mx, IRenderTypeBuffer buffers, int light, int overlay) {

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

        Optional<MiniaturizationRecipe> recipe = tile.getRecipe();
        recipe.ifPresent(rec -> {
            mx.push();

            mx.translate(0.5, 0.5, 0.5);

            // progress, ticks required
            double craftProgress = tile.getProgress();

            double progress = 1.0d - (craftProgress / (double)rec.getTicks());

            double scale = progress * (1.0f - ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 2000) + 1.0f) * 0.1f));

            mx.scale((float) scale, (float) scale, (float) scale);

            double angle = RenderTickCounter.renderTicks * (45.0f / 64.0f);
            mx.rotate(Vector3f.YP.rotationDegrees((float) angle));

            AxisAlignedBB dimensions = rec.getDimensions();
            mx.translate(-(dimensions.getXSize() / 2), -(dimensions.getYSize() / 2), -(dimensions.getZSize() / 2));


            double ySize = rec.getDimensions().getYSize();

            for(int y = 0; y < ySize; y++) {
                mx.push();
                mx.translate(0, y, 0);

                Optional<RecipeLayer> layer = rec.getLayer(y);
                layer.ifPresent(l -> {
                    l.getFilledPositions().forEach(filledPos -> {
                        mx.push();
                        mx.translate(filledPos.getX(), 0, filledPos.getZ());
                        Optional<String> component = l.getRequiredComponentKeyForPosition(filledPos);
                        Optional<BlockState> recipeComponent = rec.getRecipeComponent(component.get());

                        recipeComponent.ifPresent(state -> {
                            blockRenderer.renderBlock(state, mx, buffers, light, overlay, EmptyModelData.INSTANCE);
                        });
                        mx.pop();
                    });
                });

                mx.pop();
            }

            mx.pop();
        });
    }
}
