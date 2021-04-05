package com.robotgryphon.compactcrafting.ui.widget.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.ui.UiHelper;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;

import java.util.function.Consumer;

public class GuiTab implements IRenderable, IGuiEventListener, IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");

    private final ItemRenderer itemRenderer;
    private final FontRenderer fontRenderer;
    private TabsWidget container;
    private boolean isAlignedRight;
    private ItemStack icon;

    protected Vector2f screenPosition;
    private Consumer<GuiTab> clickHandler;
    private boolean visible;

    public GuiTab(TabsWidget container, ItemStack icon) {
        this.visible = false;
        this.container = container;
        this.isAlignedRight = false;
        this.icon = icon;

        Minecraft mc = Minecraft.getInstance();
        this.itemRenderer = mc.getItemRenderer();
        this.fontRenderer = mc.font;
    }

    public GuiTab onClicked(Consumer<GuiTab> handler) {
        this.clickHandler = handler;
        return this;
    }

    /**
     * Do not use; not fully implemented yet - aligns tab to right of tab widget
     * @return
     */
    private GuiTab onRight() {
        this.isAlignedRight = true;
        this.container.layout();
        return this;
    }

    public void setPosition(Vector2f screenPosition) {
        this.screenPosition = screenPosition;
    }

    public int getHeight() { return 28; }

    public int getWidth() {
        return 28;
    }

    public boolean isOver(double mouseX, double mouseY) {
        return UiHelper.pointInBounds(mouseX, mouseY,
                this.screenPosition.x, this.screenPosition.y,
                this.getWidth(), this.getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        container.setActiveTab(this);
        if(this.clickHandler != null)
            this.clickHandler.accept(this);

        return true;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

    }

    protected void renderTabButton(MatrixStack matrix, boolean isActive, boolean topRow, boolean rightAligned, ItemStack icon) {
        Vector2f tabPos = this.screenPosition;

        int uvU = 0;
        int uvV = 0;

        float renderPosX = tabPos.x;
        float renderPosY = tabPos.y;

        if (isActive) {
            uvV += 32;
        }

        if (rightAligned) {
            if((tabPos.x + getWidth()) < container.bounds.getWidth())
                // use "middle" tab UV index
                uvU = 28;
            else
                // use "right" tab UV index
                uvU = 28 * 2;
        } else if (tabPos.x > 0) {
            // use "middle" tab UV index
            uvU = 28;
        }

        if (topRow) {
            renderPosY = renderPosY - 28;
        } else {
            uvV += 64;
            renderPosY -= 4;
        }

        RenderSystem.color3f(1F, 1F, 1F); //Forge: Reset color in case Items change it.
        RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
        AbstractGui.blit(matrix, (int) renderPosX, (int) renderPosY, 0, (float) uvU, (float) uvV, 28, 32, 256, 256);

        RenderSystem.pushMatrix();
        Matrix4f m = matrix.last().pose();
        RenderSystem.multMatrix(m);

        int iconRenderX = (int) renderPosX + 6;
        int iconRenderY = (int) renderPosY + 8 + (topRow ? 1 : -1);
        RenderSystem.enableRescaleNormal();

        this.itemRenderer.renderAndDecorateItem(icon, iconRenderX, iconRenderY);
        this.itemRenderer.renderGuiItemDecorations(this.fontRenderer, icon, iconRenderX, iconRenderY);

        RenderSystem.popMatrix();
    }

    @Override
    public void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible && container.isActive(this)) {
            // Renders active tab above rest of background
            matrixStack.pushPose();
            Minecraft.getInstance().getTextureManager().bind(TEXTURE);
            renderTabButton(matrixStack, true,
                    container.getSide() == EnumTabWidgetSide.TOP,
                    isAlignedRight, icon);

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible && !container.isActive(this)) {
            Minecraft.getInstance().getTextureManager().bind(TEXTURE);
            renderTabButton(matrixStack, false,
                    container.getSide() == EnumTabWidgetSide.TOP,
                    isAlignedRight, this.icon);
        }
    }

    public boolean isOnRight() {
        return this.isAlignedRight;
    }

    public void setVisible(boolean b) {
        this.visible = b;
    }

    @Override
    public String toString() {
        if(icon != null)
            return String.format("GuiTab {icon: %s}", icon.toString());

        return "GuiTab {none}";
    }

    public boolean isVisible() {
        return this.visible;
    }
}
