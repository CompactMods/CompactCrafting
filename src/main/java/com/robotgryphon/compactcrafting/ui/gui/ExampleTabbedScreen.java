package com.robotgryphon.compactcrafting.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.ui.container.ExampleTabbedContainer;
import com.robotgryphon.compactcrafting.ui.widget.WidgetHolder;
import com.robotgryphon.compactcrafting.ui.widget.tab.EnumTabWidgetSide;
import com.robotgryphon.compactcrafting.ui.widget.tab.GuiTab;
import com.robotgryphon.compactcrafting.ui.widget.tab.TabsWidget;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class ExampleTabbedScreen extends TabHostedContainerScreen<ExampleTabbedContainer> {

    private final PlayerEntity player;

    public ExampleTabbedScreen(ExampleTabbedContainer c, PlayerInventory inv, ITextComponent name) {
        super(c, inv, name);

        this.GUI = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/edit-screen.png");
        this.player = inv.player;

        this.imageHeight = 180;
        this.imageWidth = 176;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void initTabs() {
        this.widgets = new WidgetHolder();

        TabsWidget tabsTop = new TabsWidget(this)
                .withSide(EnumTabWidgetSide.TOP)
                .onTabChanged(this::handleTabChanged);

        List<GuiTab> oTabs = new ArrayList<GuiTab>();
        for(int i = 0; i < 15; i++)
            oTabs.add(new GuiTab(tabsTop, new ItemStack(Items.GLASS)));

        GuiTab pro = new GuiTab(tabsTop, new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get()));

        oTabs.add(pro);
        tabsTop.addTabs(oTabs);
        this.widgets.add(tabsTop);
    }

    private void handleTabChanged(GuiTab tab) {
        // change screen here, use NetworkHooks
        minecraft
                .getSoundManager()
                .play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f, .10f));
    }


    @Override
    protected void renderLabels(MatrixStack ms, int mouseX, int mouseY) {
        // this.font.draw(ms, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(ms, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
