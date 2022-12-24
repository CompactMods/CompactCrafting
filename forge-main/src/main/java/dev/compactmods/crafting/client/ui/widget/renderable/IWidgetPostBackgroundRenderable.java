package dev.compactmods.crafting.client.ui.widget.renderable;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IWidgetPostBackgroundRenderable {
    void renderPostBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
