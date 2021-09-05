package dev.compactmods.crafting.ui.widget.renderable;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IWidgetPostBackgroundRenderable {
    void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
