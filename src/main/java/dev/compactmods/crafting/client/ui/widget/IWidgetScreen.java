package dev.compactmods.crafting.client.ui.widget;

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.world.phys.Vec2;

public interface IWidgetScreen extends Tickable {
    Vec2 getScreenSize();
}
