package dev.compactmods.crafting.client.ui.widget;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WidgetScreen extends Screen {

    protected WidgetHolder widgets;

    protected WidgetScreen(Component title) {
        super(title);
        this.widgets = new WidgetHolder();
    }
}
