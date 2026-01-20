package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GhostRenderer {
    public static void render(BlockState state, @Nullable BlockPos pos, PoseStack poseStack, float alpha, float scale) {
        final Minecraft mc = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();

        poseStack.pushPose();
        {
            Vec3 projectedView = mainCamera.position();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            GhostRenderer.renderTransparentBlock(state, pos, poseStack, buffers, alpha, scale);
        }
        poseStack.popPose();
    }

    public static void renderTransparentBlock(BlockState state, BlockPos pos, PoseStack matrix, MultiBufferSource buffer, float alpha, float scale) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockColors colors = mc.getBlockColors();

        VertexConsumer builder = buffer.getBuffer(RenderTypes.translucentMovingBlock());
        final BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        final var model = dispatcher.getBlockModel(state);

        float offset = (1 - scale) / 2f;

        matrix.pushPose();
        matrix.translate(Vec3.atLowerCornerWithOffset(pos, offset, offset, offset));
        matrix.scale(scale, scale, scale);

//        dispatcher.renderSingleBlock(state, matrix, buffer, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY,
//                EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);

        List<BlockModelPart> parts = new ArrayList<>();
        model.collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, state, mc.level.getRandom(), parts);

        parts.forEach(part -> {
            for(var dir : Direction.values()) {
                for(var quad : part.getQuads(dir)) {
                    addQuad(state, matrix, colors, builder, quad, alpha);
                }
            }

            for(var quad : part.getQuads(null)) {
                addQuad(state, matrix, colors, builder, quad, alpha);
            }
        });

        matrix.popPose();


    }

    private static void addQuad(BlockState state, PoseStack poseStack, BlockColors colors, VertexConsumer builder, BakedQuad quad, float alpha) {
        int color = quad.isTinted() ? colors.getColor(state, EmptyBlockAndTintGetter.INSTANCE, null, quad.tintIndex()) :
                CommonColors.WHITE;

        final float red = ARGB.red(color) / 255f;
        final float green = ARGB.green(color) / 255f;
        final float blue = ARGB.blue(color) / 255f;

        final float trueAlpha = Mth.clamp(alpha, 0.1f, 1f);

        builder.putBulkData(poseStack.last(), quad, red, green, blue, trueAlpha, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY);
    }
}
