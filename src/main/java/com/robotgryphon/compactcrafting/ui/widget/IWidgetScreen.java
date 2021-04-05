package com.robotgryphon.compactcrafting.ui.widget;

import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.math.vector.Vector2f;

public interface IWidgetScreen extends IScreen {
    Vector2f getScreenSize();
    Vector2f getScreenOffset();

    Rectangle2d getBounds();
}
