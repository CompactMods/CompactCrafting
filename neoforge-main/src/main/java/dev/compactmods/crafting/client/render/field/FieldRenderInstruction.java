package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface FieldRenderInstruction {
    void draw(PoseStack poseStack, MultiBufferSource.BufferSource buffers);
}
