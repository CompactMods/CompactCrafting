package dev.compactmods.crafting.field.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.GhostRenderer;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Optional;

public class CraftingPreviewRenderer {
    public static void render(IMiniaturizationRecipe recipe, double progress, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        if(recipe == null)
            return;
        
        if(progress >= recipe.getCraftingTime()) {
            return;
        }

        stack.pushPose();

        stack.translate(0.5, 0.5, 0.5);

        try {
            // progress, ticks required
            double craftProgress = progress;


            double scale = MathUtil.calculateFieldScale(craftProgress + 3, recipe.getCraftingTime());

            stack.scale((float) scale, (float) scale, (float) scale);

            long gameTime = Minecraft.getInstance().level.getGameTime();
            double angle = (gameTime  % 360.0) * 2.0d;
            stack.mulPose(Axis.YP.rotationDegrees((float) angle));

            AABB dimensions = recipe.getDimensions();
            stack.translate(-(dimensions.getXsize() / 2), -(dimensions.getYsize() / 2), -(dimensions.getZsize() / 2));

            double ySize = recipe.getDimensions().getYsize();

            for (int y = 0; y < ySize; y++) {
                stack.pushPose();
                stack.translate(0, y, 0);

                Optional<IRecipeLayer> layer = recipe.getLayer(y);
                int finalY = y;
                layer.ifPresent(l -> {
                    AABB layerBounds = BlockSpaceUtil.getLayerBounds(recipe.getDimensions(), finalY);
                    BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
                        stack.pushPose();
                        stack.translate(filledPos.getX(), 0, filledPos.getZ());

                        BlockPos zeroedPos = filledPos.below(finalY);
                        l.getComponentForPosition(zeroedPos)
                                .flatMap(recipe.getComponents()::getBlock)
                                .ifPresent(comp -> renderSingleBlock(stack, buffers, blockRenderer, comp, craftProgress, recipe.getCraftingTime()));

                        stack.popPose();
                    });
                });

                stack.popPose();
            }
        }

        catch(Exception ex) {
            ex.printStackTrace();
        }

        stack.popPose();
    }

    private static void renderSingleBlock(PoseStack stack, MultiBufferSource buffers, BlockRenderDispatcher blockRenderer, IRecipeBlockComponent comp, double progress, double craftingTime) {
        BlockState state = comp.getRenderState();
        
        double progressPercent = Math.min(progress / craftingTime, 1.0);
        
        stack.pushPose();
        
        stack.translate(0.5, 0.5, 0.5);
        
        long gameTime = Minecraft.getInstance().level.getGameTime();
        double spinSpeed = 2.0d + (progressPercent * 8.0d);
        double blockAngle = (gameTime % 360.0) * spinSpeed;
        stack.mulPose(Axis.YP.rotationDegrees((float) blockAngle));
        
        double individualScale = 1.0 - (progressPercent * 0.3);
        stack.scale((float) individualScale, (float) individualScale, (float) individualScale);
        
        stack.translate(-0.5, -0.5, -0.5);

        final var mc = Minecraft.getInstance();
        final var colors = mc.getBlockColors();
        final var builder = buffers.getBuffer(CCRenderTypes.PHANTOM);
        final var dispatcher = mc.getBlockRenderer();
        final var model = dispatcher.getBlockModel(state);
        
        if (model != mc.getModelManager().getMissingModel()) {
            final float alpha = 0.9f; // 90% opaque (10% transparent)
            
            for (var dir : Direction.values()) {
                model.getQuads(state, dir, mc.level.random, ModelData.EMPTY, null)
                    .forEach(quad -> {
                        int color = quad.isTinted() ? colors.getColor(state, mc.level, BlockPos.ZERO, quad.getTintIndex()) :
                                FastColor.ARGB32.color(255, 255, 255, 255);
                        
                        final float red = FastColor.ARGB32.red(color) / 255f;
                        final float green = FastColor.ARGB32.green(color) / 255f;
                        final float blue = FastColor.ARGB32.blue(color) / 255f;
                        
                        builder.putBulkData(stack.last(), quad, red, green, blue, alpha, 
                            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false);
                    });
            }
            
            model.getQuads(state, null, mc.level.random, ModelData.EMPTY, null)
                .forEach(quad -> {
                    int color = quad.isTinted() ? colors.getColor(state, mc.level, BlockPos.ZERO, quad.getTintIndex()) :
                            FastColor.ARGB32.color(255, 255, 255, 255);
                    
                    final float red = FastColor.ARGB32.red(color) / 255f;
                    final float green = FastColor.ARGB32.green(color) / 255f;
                    final float blue = FastColor.ARGB32.blue(color) / 255f;
                    
                    builder.putBulkData(stack.last(), quad, red, green, blue, alpha, 
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false);
                });
        }
        stack.popPose();
    }
}
