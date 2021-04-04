package com.robotgryphon.compactcrafting.ui.widget.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GenericTabsWidget extends WidgetBase implements
        IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {
    private final ItemRenderer itemRenderer;
    protected List<GenericTab> tabs;
    private Screen screen;
    private EnumTabWidgetSide screenSide;
    private int activeTab = 0;

    public GenericTabsWidget(Screen screen, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.screen = screen;
        this.tabs = new ArrayList<>();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.screenSide = EnumTabWidgetSide.TOP;
    }

    public GenericTabsWidget withSide(EnumTabWidgetSide side) {
        this.screenSide = side;
        return this;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int tabPage = 0;
        int numTabs = this.width / 28;

        int start = tabPage * numTabs;
        int end = Math.min(ItemGroup.TABS.length, ((tabPage + 1) * numTabs));
        if (tabPage != 0) start += 2;

        matrixStack.pushPose();
        matrixStack.translate(this.x, this.y, 0);

        for (int idx = start; idx < end; idx++) {
            // Skip active tab for later
            if (idx == activeTab)
                continue;

            ItemGroup ig = ItemGroup.TABS[idx];

            screen.getMinecraft().getTextureManager().bind(ig.getTabsImage());
            renderTabButton(matrixStack, idx == activeTab,
                    this.screenSide == EnumTabWidgetSide.TOP,
                    ig.getColumn(),
                    ig.isAlignedRight(), ig.getIconItem()
            );
        }

        matrixStack.popPose();
    }

    @Override
    public void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Renders active tab above rest of background
        matrixStack.pushPose();
        matrixStack.translate(this.x, this.y, 0);
        ItemGroup ig = ItemGroup.TABS[activeTab];

        screen.getMinecraft().getTextureManager().bind(ig.getTabsImage());
        renderTabButton(matrixStack, true, this.screenSide == EnumTabWidgetSide.TOP, ig.getColumn(), ig.isAlignedRight(), ig.getIconItem());

        matrixStack.popPose();
    }

    protected void renderTabButton(MatrixStack matrix, boolean isActive, boolean topRow, int tabColumn, boolean rightAligned, ItemStack icon) {
        int uvU = tabColumn * 28;
        int uvV = 0;

        int renderPosX = tabColumn * 28;
        int renderPosY = 0;

        if (isActive) {
            uvV += 32;
        }

        int NUM_POSS_TABS = (this.width / 28);
        if (rightAligned) {
            int rightEdge = this.width;
            renderPosX = rightEdge - (28 * (NUM_POSS_TABS - tabColumn));
        } else if (tabColumn > 0) {
            renderPosX += tabColumn;
        }

        if (topRow) {
            renderPosY = renderPosY - 28;
        } else {
            uvV += 64;
            renderPosY += (this.height - 4);
        }

        RenderSystem.color3f(1F, 1F, 1F); //Forge: Reset color in case Items change it.
        RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
        screen.blit(matrix, renderPosX, renderPosY, uvU, uvV, 28, 32);

        int iconRenderX = this.x + renderPosX + 6;
        int iconRenderY = this.y + renderPosY + 8 + (topRow ? 1 : -1);
        RenderSystem.enableRescaleNormal();

        this.itemRenderer.renderAndDecorateItem(icon, iconRenderX, iconRenderY);
        this.itemRenderer.renderGuiItemDecorations(screen.getMinecraft().font, icon, iconRenderX, iconRenderY);
    }
}
