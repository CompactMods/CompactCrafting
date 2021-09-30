package dev.compactmods.crafting.client.ui.widget;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class ContainerWidgetScreen<T extends Container> extends ContainerScreen<T> {
    protected WidgetHolder widgets;
    public ContainerWidgetScreen(T screen, PlayerInventory inv, ITextComponent title) {
        super(screen, inv, title);
        this.widgets = new WidgetHolder();
    }
}
