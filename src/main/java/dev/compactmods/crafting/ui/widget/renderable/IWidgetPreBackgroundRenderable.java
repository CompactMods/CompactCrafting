package dev.compactmods.crafting.ui.widget.renderable;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IWidgetPreBackgroundRenderable {
    void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
