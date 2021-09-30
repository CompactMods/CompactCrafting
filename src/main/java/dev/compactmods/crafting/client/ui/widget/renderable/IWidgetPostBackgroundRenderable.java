package dev.compactmods.crafting.client.ui.widget.renderable;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IWidgetPostBackgroundRenderable {
    void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
