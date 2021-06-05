package com.robotgryphon.compactcrafting.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.ui.container.TestContainer;
import com.robotgryphon.compactcrafting.ui.widget.ContainerWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.IWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import com.robotgryphon.compactcrafting.ui.widget.WidgetHolder;
import com.robotgryphon.compactcrafting.ui.widget.tab.EnumTabWidgetSide;
import com.robotgryphon.compactcrafting.ui.widget.tab.GuiTab;
import com.robotgryphon.compactcrafting.ui.widget.tab.TabsWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TestScreen extends ContainerWidgetScreen<TestContainer> implements IWidgetScreen {

    private ResourceLocation GUI = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/edit-screen.png");
    private final PlayerEntity player;

    public TestScreen(TestContainer c, PlayerInventory inv, ITextComponent name) {
        super(c, inv, name);
        this.player = inv.player;

        this.imageHeight = 180;
        this.imageWidth = 176;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        this.widgets = new WidgetHolder();

        TabsWidget tabsTop = new TabsWidget(this, imageWidth, 28 + 20)
                .withSide(EnumTabWidgetSide.TOP);

        new GuiTab(tabsTop, new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get()))
                .onClicked((t) -> {
                    player.displayClientMessage(new StringTextComponent("hi!"), true);
                });

        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));
        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));
        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));
        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));
        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));
        new GuiTab(tabsTop, new ItemStack(Items.REDSTONE));


        this.widgets.add(tabsTop);

        TabsWidget tabsBottom = new TabsWidget(this, imageWidth, 28)
                .withSide(EnumTabWidgetSide.BOTTOM);

        new GuiTab(tabsBottom, new ItemStack(Items.REDSTONE));
        this.widgets.add(tabsBottom);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.render(ms, mouseX, mouseY, partialTicks);
        ms.popPose();
    }

    @Override
    protected void renderLabels(MatrixStack ms, int mouseX, int mouseY) {
        // this.font.draw(ms, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(ms, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.renderPreBackground(ms, mouseX, mouseY, partialTicks);
        ms.popPose();

        this.minecraft.getTextureManager().bind(GUI);
        this.blit(ms, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.renderPostBackground(ms, mouseX, mouseY, partialTicks);
        ms.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        double relX = mouseX - leftPos;
        double relY = mouseY - topPos;

        for (WidgetBase w : widgets.getWidgets()) {
            if (w.isMouseOver(relX, relY)) {
                boolean handled = w.mouseClicked(relX, relY, button);
                if (handled)
                    return true;
            }
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public Vector2f getScreenSize() {
        return new Vector2f(this.imageWidth, this.imageHeight);
    }
}
