package com.robotgryphon.compactcrafting.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public abstract class WidgetBase implements IRenderable, IGuiEventListener {

    protected WidgetBase() {

    }

    @Override
    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
