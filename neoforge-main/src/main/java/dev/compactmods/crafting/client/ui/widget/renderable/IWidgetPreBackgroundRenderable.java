package dev.compactmods.crafting.client.ui.widget.renderable;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IWidgetPreBackgroundRenderable {
    void renderPreBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
