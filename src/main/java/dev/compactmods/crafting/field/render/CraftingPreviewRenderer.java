package dev.compactmods.crafting.field.render;

import java.util.Optional;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import dev.compactmods.crafting.util.MathUtil;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import com.mojang.math.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CraftingPreviewRenderer {
    public static void render(IMiniaturizationRecipe recipe, double progress, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        if(recipe == null)
            return;

        stack.pushPose();

        stack.translate(0.5, 0.5, 0.5);

        try {
            // progress, ticks required
            double craftProgress = progress;


            double scale = MathUtil.calculateFieldScale(craftProgress + 3, recipe.getCraftingTime());

            stack.scale((float) scale, (float) scale, (float) scale);

            long gameTime = Minecraft.getInstance().level.getGameTime();
            double angle = (gameTime  % 360.0) * 2.0d;
            stack.mulPose(Vector3f.YP.rotationDegrees((float) angle));

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
                                .ifPresent(comp -> {
                                    // TODO - Render switching
                                    BlockState state1 = comp.getRenderState();
                                    blockRenderer.renderSingleBlock(state1, stack, buffers, light, overlay, EmptyModelData.INSTANCE);
                                });

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
}
