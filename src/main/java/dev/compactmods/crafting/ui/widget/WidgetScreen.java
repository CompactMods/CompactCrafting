package dev.compactmods.crafting.ui.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class WidgetScreen extends Screen {

    protected WidgetHolder widgets;

    protected WidgetScreen(ITextComponent title) {
        super(title);
        this.widgets = new WidgetHolder();
    }
}
