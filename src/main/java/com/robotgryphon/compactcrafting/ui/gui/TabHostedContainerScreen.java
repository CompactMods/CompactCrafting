package com.robotgryphon.compactcrafting.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.ui.widget.ContainerWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.IWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import com.robotgryphon.compactcrafting.ui.widget.tab.TabsWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public abstract class TabHostedContainerScreen<T extends Container>
        extends ContainerWidgetScreen<T>
        implements IWidgetScreen {

    protected ResourceLocation GUI;
    protected TabsWidget tabs;

    public TabHostedContainerScreen(T screen, PlayerInventory inv, ITextComponent title) {
        super(screen, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.initTabs();
    }

    protected abstract void initTabs();

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

        return false;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);

        // Get relative coordinates for mouse
        int wMouseX = mouseX - leftPos;
        int wMouseY = mouseY - topPos;

        this.widgets.render(ms, wMouseX, wMouseY, partialTicks);
        ms.popPose();
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);

        // Get relative coordinates for mouse
        int wMouseX = mouseX - leftPos;
        int wMouseY = mouseY - topPos;

        this.widgets.renderPreBackground(ms, wMouseX, wMouseY, partialTicks);
        ms.popPose();

        this.minecraft.getTextureManager().bind(GUI);
        this.blit(ms, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.renderPostBackground(ms, mouseX, mouseY, partialTicks);
        ms.popPose();
    }


    @Override
    public Vector2f getScreenSize() {
        return new Vector2f(this.imageWidth, this.imageHeight);
    }

    @Override
    public Vector2f getScreenOffset() {
        return new Vector2f(leftPos, topPos);
    }

    @Override
    public Rectangle2d getBounds() {
        return new Rectangle2d(leftPos, topPos, imageWidth, imageHeight);
    }
}
