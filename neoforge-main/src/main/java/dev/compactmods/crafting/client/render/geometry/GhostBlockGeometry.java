package dev.compactmods.crafting.client.render.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record GhostBlockGeometry(BlockPos position, BlockState state, float alpha,
                                 float scale) implements SubmitNodeCollector.CustomGeometryRenderer {

    @Override
    public void render(PoseStack.Pose pose, @NonNull VertexConsumer builder) {
        final var mc = Minecraft.getInstance();
        final BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        final BlockColors colors = mc.getBlockColors();

        final var model = dispatcher.getBlockModel(state);

        float offset = (1 - scale) / 2f;

        final var realPosition = Vec3.atLowerCornerWithOffset(position, offset, offset, offset).toVector3f();
        pose.translate(realPosition.x, realPosition.y, realPosition.z);
        pose.scale(scale, scale, scale);

        List<BlockModelPart> parts = new ArrayList<>();
        model.collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, state, RandomSource.create(), parts);

        parts.forEach(part -> {
            for (var dir : Direction.values()) {
                for (var quad : part.getQuads(dir)) {
                    addQuad(state, pose, colors, builder, quad, alpha);
                }
            }

            for (var quad : part.getQuads(null)) {
                addQuad(state, pose, colors, builder, quad, alpha);
            }
        });
    }

    private static void addQuad(BlockState state, PoseStack.Pose pose, BlockColors colors, VertexConsumer builder, BakedQuad quad, float alpha) {
        int color = quad.isTinted() ? colors.getColor(state, EmptyBlockAndTintGetter.INSTANCE, null, quad.tintIndex()) :
                CommonColors.WHITE;

        final float red = ARGB.redFloat(color);
        final float green = ARGB.greenFloat(color);
        final float blue = ARGB.blueFloat(color);

        final float trueAlpha = Mth.clamp(alpha, 0.1f, 1f);

        builder.putBulkData(pose, quad, red, green, blue, trueAlpha, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY);
    }
}
