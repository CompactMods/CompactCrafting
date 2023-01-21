package dev.compactmods.crafting.client.ui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Widget;

public abstract class WidgetBase implements Widget, GuiEventListener {
    protected final int width;
    protected final int height;

    protected WidgetBase(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public abstract void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
