package dev.compactmods.crafting.client.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public abstract class WidgetBase implements IRenderable, IGuiEventListener {
    protected final int width;
    protected final int height;

    protected WidgetBase(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
