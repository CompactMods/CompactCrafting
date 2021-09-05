package dev.compactmods.crafting.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.compactmods.crafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import dev.compactmods.crafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.gui.IRenderable;

import java.util.ArrayList;
import java.util.List;

public class WidgetHolder implements IRenderable {
    protected List<WidgetBase> widgets;

    public WidgetHolder() {
        this.widgets = new ArrayList<>();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        for(WidgetBase w : widgets) {
            w.render(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public void add(WidgetBase widget) {
        this.widgets.add(widget);
    }

    public void renderPreBackground(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPreBackgroundRenderable)
                ((IWidgetPreBackgroundRenderable) w).renderPreBackground(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public void renderPostBackground(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPostBackgroundRenderable)
                ((IWidgetPostBackgroundRenderable) w).renderPostBackground(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public List<WidgetBase> getWidgets() {
        return this.widgets;
    }
}
