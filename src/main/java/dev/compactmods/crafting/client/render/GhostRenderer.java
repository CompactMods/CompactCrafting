package dev.compactmods.crafting.client.render;

import java.util.Arrays;
import java.util.Random;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GhostRenderer {
    public static void renderTransparentBlock(BlockState state, MatrixStack matrix, IRenderTypeBuffer buffer) {
        IVertexBuilder builder = buffer.getBuffer(CCRenderTypes.PHANTOM);
        matrix.translate(0.1F, 0.1F, 0.1F);
        matrix.scale(0.8F, 0.8F, 0.8F);
        IBakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state.getBlockState());
        if(model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            Arrays.stream(Direction.values())
                    .flatMap(direction -> model.getQuads(state.getBlockState(), direction, new Random(42L), EmptyModelData.INSTANCE).stream())
                    .forEach(quad -> builder.addVertexData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY));
            model.getQuads(state.getBlockState(), null, new Random(42L), EmptyModelData.INSTANCE)
                    .forEach(quad -> builder.addVertexData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY));
        }
    }
}
