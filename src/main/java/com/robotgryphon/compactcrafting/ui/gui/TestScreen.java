package com.robotgryphon.compactcrafting.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.ui.container.TestContainer;
import com.robotgryphon.compactcrafting.ui.widget.ContainerWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetHolder;
import com.robotgryphon.compactcrafting.ui.widget.tab.EnumTabWidgetSide;
import com.robotgryphon.compactcrafting.ui.widget.tab.GenericTabsWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TestScreen extends ContainerWidgetScreen<TestContainer> {
    private GenericTabsWidget tabs;
    private GenericTabsWidget tabsBottom;

    private ResourceLocation GUI = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/edit-screen.png");

    public TestScreen(TestContainer c, PlayerInventory inv, ITextComponent name) {
        super(c, inv, name);
        this.imageHeight = 180;
        this.imageWidth = 176;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        this.widgets = new WidgetHolder();
        
        GenericTabsWidget tabsTop = new GenericTabsWidget(this, getGuiLeft(), topPos, imageWidth, 28)
                .withSide(EnumTabWidgetSide.TOP);

        this.widgets.add(tabsTop);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        this.widgets.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(MatrixStack ms, int mouseX, int mouseY) {
        // this.font.draw(ms, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(ms, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        this.widgets.renderPreBackground(ms, mouseX, mouseY, partialTicks);

        this.minecraft.getTextureManager().bind(GUI);
        this.blit(ms, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        this.widgets.renderPostBackground(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
