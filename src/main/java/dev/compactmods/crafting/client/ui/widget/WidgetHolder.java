package dev.compactmods.crafting.client.ui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.gui.components.Widget;

import java.util.ArrayList;
import java.util.List;

public class WidgetHolder implements Widget {
    protected List<WidgetBase> widgets;

    public WidgetHolder() {
        this.widgets = new ArrayList<>();
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        for(WidgetBase w : widgets) {
            w.render(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public void add(WidgetBase widget) {
        this.widgets.add(widget);
    }

    public void renderPreBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPreBackgroundRenderable)
                ((IWidgetPreBackgroundRenderable) w).renderPreBackground(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public void renderPostBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPostBackgroundRenderable)
                ((IWidgetPostBackgroundRenderable) w).renderPostBackground(matrix, mouseX, mouseY, partialTicks);
        }
    }

    public List<WidgetBase> getWidgets() {
        return this.widgets;
    }
}
