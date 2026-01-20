package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;

public interface FieldRenderInstruction {
    void draw(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers);
}
