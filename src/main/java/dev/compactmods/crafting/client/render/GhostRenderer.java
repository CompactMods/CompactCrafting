package dev.compactmods.crafting.client.render;

import javax.annotation.Nullable;
import java.util.Random;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GhostRenderer {
    public static void renderTransparentBlock(BlockState state, @Nullable BlockPos pos, MatrixStack matrix, IRenderTypeBuffer buffer) {
        renderTransparentBlock(state, pos, matrix, buffer, 100);
    }

    public static void renderTransparentBlock(BlockState state, @Nullable BlockPos pos, MatrixStack matrix, IRenderTypeBuffer buffer, int ticksLeft) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockColors colors = mc.getBlockColors();

        // clamp b/n 0-100, effective range 0 - 0.9f
        final float alpha = ticksLeft >= 100 ? 0.9f : 0.9f * Math.max(ticksLeft / 100f, .1f);

        IVertexBuilder builder = buffer.getBuffer(CCRenderTypes.PHANTOM);
        IBakedModel model = mc.getBlockRenderer().getBlockModel(state.getBlockState());
        if (model != mc.getModelManager().getMissingModel()) {
            for(Direction dir : Direction.values())
                model.getQuads(state.getBlockState(), dir, new Random(42L), EmptyModelData.INSTANCE)
                        .forEach(quad -> addQuad(state, pos, matrix, mc, colors, builder, quad, alpha));

            model.getQuads(state.getBlockState(), null, new Random(42L), EmptyModelData.INSTANCE)
                    .forEach(quad -> addQuad(state, pos, matrix, mc, colors, builder, quad, alpha));
        }
    }

    private static void addQuad(BlockState state, @Nullable BlockPos pos, MatrixStack matrix, Minecraft mc, BlockColors colors, IVertexBuilder builder, BakedQuad quad, float alpha) {
        int color = quad.isTinted() ? colors.getColor(state, mc.level, pos, quad.getTintIndex()) :
                ColorHelper.PackedColor.color(255, 255, 255, 255);

        final float red = ColorHelper.PackedColor.red(color) / 255f;
        final float green = ColorHelper.PackedColor.green(color) / 255f;
        final float blue = ColorHelper.PackedColor.blue(color) / 255f;

        final float trueAlpha = MathHelper.clamp(0.01f, alpha, 0.06f);
        builder.addVertexData(matrix.last(), quad,
                red,
                green,
                blue,
                trueAlpha,
                LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
    }
}
