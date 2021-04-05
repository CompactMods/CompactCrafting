package com.robotgryphon.compactcrafting.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.Rectangle2d;

public abstract class WidgetBase implements IRenderable, IGuiEventListener {

    protected WidgetBase() {

    }

    public abstract void layout(Rectangle2d parentBounds);

    @Override
    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    public abstract Rectangle2d getBounds();

    public abstract void setPosition(int x, int y);
}
