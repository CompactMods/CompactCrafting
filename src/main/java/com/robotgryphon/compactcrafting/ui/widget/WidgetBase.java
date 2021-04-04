package com.robotgryphon.compactcrafting.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public abstract class WidgetBase implements IRenderable, IGuiEventListener {

    protected final int x, y, width, height;

    protected WidgetBase(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    @Override
    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
