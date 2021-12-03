package dev.compactmods.crafting.client.ui.widget;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

public abstract class ContainerWidgetScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected WidgetHolder widgets;
    public ContainerWidgetScreen(T screen, Inventory inv, Component title) {
        super(screen, inv, title);
        this.widgets = new WidgetHolder();
    }
}
