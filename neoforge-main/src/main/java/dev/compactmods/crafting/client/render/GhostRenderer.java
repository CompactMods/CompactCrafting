package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GhostRenderer {
    public static void render(BlockState state, @Nullable BlockPos pos, PoseStack matrixStack) {
        final Minecraft mc = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        final ClientLevel level = mc.level;

        matrixStack.pushPose();
        {
            Vec3 projectedView = mainCamera.position();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            matrixStack.pushPose();
            {
                matrixStack.translate(
                        (double) pos.getX() + 0.05,
                        (double) pos.getY() + 0.05,
                        (double) pos.getZ() + 0.05
                );

                matrixStack.scale(.9f, .9f, .9f);

                GhostRenderer.renderTransparentBlock(state, pos, matrixStack, buffers, 100);
            }
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    public static void renderTransparentBlock(BlockState state, @Nullable BlockPos pos, PoseStack matrix) {
        final var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderTransparentBlock(state, pos, matrix, buffers, 100);
    }

    public static void renderTransparentBlock(BlockState state, @Nullable BlockPos pos, PoseStack matrix, MultiBufferSource buffer) {
        renderTransparentBlock(state, pos, matrix, buffer, 100);
    }

    public static void renderTransparentBlock(BlockState state, @Nullable BlockPos pos, PoseStack matrix, MultiBufferSource buffer, int ticksLeft) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockColors colors = mc.getBlockColors();

        // clamp b/n 0-100, effective range 0 - 0.9f
        final float alpha = ticksLeft >= 100 ? 0.9f : 0.9f * Math.max(ticksLeft / 100f, .1f);

        VertexConsumer builder = buffer.getBuffer(RenderTypes.translucentMovingBlock());
        final BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        final var model = dispatcher.getBlockModel(state);

        if (model != mc.getModelManager().getMissingBlockStateModel()) {
            List<BlockModelPart> parts = new ArrayList<>();
            model.collectParts(mc.level, pos, state, mc.level.getRandom(), parts);

            parts.forEach(part -> {
                for(var dir : Direction.values()) {
                    for(var quad : part.getQuads(dir)) {
                        addQuad(state, pos, matrix, mc, colors, builder, quad, alpha);
                    }
                }
            });
        }
    }

    private static void addQuad(BlockState state, @Nullable BlockPos pos, PoseStack matrix, Minecraft mc, BlockColors colors, VertexConsumer builder, BakedQuad quad, float alpha) {
        int color = quad.isTinted() ? colors.getColor(state, mc.level, pos, quad.tintIndex()) :
                ARGB.color(255, 255, 255, 255);

        final float red = ARGB.red(color) / 255f;
        final float green = ARGB.green(color) / 255f;
        final float blue = ARGB.blue(color) / 255f;

        final float trueAlpha = Mth.clamp(0.01f, alpha, 0.06f);

        builder.putBulkData(matrix.last(), quad, red, green, blue, trueAlpha, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY);
    }
}
