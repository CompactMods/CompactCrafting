package dev.compactmods.crafting.client.ui.widget.tab;

import java.util.function.Consumer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ui.UiHelper;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class GuiTab implements Widget, GuiEventListener, IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");

    private final ItemRenderer itemRenderer;
    private final Font fontRenderer;
    private TabsWidget container;
    private boolean isAlignedRight;
    private ItemStack icon;

    protected Vec2 screenPosition;
    private Consumer<GuiTab> clickHandler;

    public GuiTab(TabsWidget container, ItemStack icon) {
        this.container = container;
        this.isAlignedRight = false;
        this.icon = icon;

        Minecraft mc = Minecraft.getInstance();
        this.itemRenderer = mc.getItemRenderer();
        this.fontRenderer = mc.font;

        this.container.addTab(this);
    }

    public GuiTab onClicked(Consumer<GuiTab> handler) {
        this.clickHandler = handler;
        return this;
    }

    public GuiTab onRight() {
        this.isAlignedRight = true;
        this.container.layout();
        return this;
    }

    public void setPosition(Vec2 screenPosition) {
        this.screenPosition = screenPosition;
    }

    public int getHeight() { return 28; }

    public int getWidth() {
        return 28;
    }

    public boolean isOver(double mouseX, double mouseY) {
        return UiHelper.pointInBounds(mouseX, mouseY, this.screenPosition.x, this.screenPosition.y, this.getWidth(), this.getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        container.setActiveTab(this);
        if(this.clickHandler != null)
            this.clickHandler.accept(this);

        return true;
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {

    }

    protected void renderTabButton(PoseStack matrix, boolean isActive, boolean topRow, boolean rightAligned, ItemStack icon) {
        Vec2 tabPos = this.screenPosition;

        int uvU = 0;
        int uvV = 0;

        float renderPosX = tabPos.x;
        float renderPosY = tabPos.y;

        if (isActive) {
            uvV += 32;
        }

        if (rightAligned) {
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

        RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
        GuiComponent.blit(matrix, (int) renderPosX, (int) renderPosY, 0, (float) uvU, (float) uvV, 28, 32, 256, 256);

//        RenderSystem.pushMatrix();
//        Matrix4f m = matrix.last().pose();
//        RenderSystem.multMatrix(m);
//
//        int iconRenderX = (int) renderPosX + 6;
//        int iconRenderY = (int) renderPosY + 8 + (topRow ? 1 : -1);
//        RenderSystem.enableRescaleNormal();
//
//        this.itemRenderer.renderAndDecorateItem(icon, iconRenderX, iconRenderY);
//        this.itemRenderer.renderGuiItemDecorations(this.fontRenderer, icon, iconRenderX, iconRenderY);
//
//        RenderSystem.popMatrix();
    }

    @Override
    public void renderPostBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (container.isActive(this)) {
            // Renders active tab above rest of background
            matrixStack.pushPose();
            Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE);
            renderTabButton(matrixStack, true,
                    container.getSide() == EnumTabWidgetSide.TOP,
                    isAlignedRight, icon);

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPreBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!container.isActive(this)) {
            Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE);
            renderTabButton(matrixStack, false,
                    container.getSide() == EnumTabWidgetSide.TOP,
                    isAlignedRight, this.icon);
        }
    }

    public boolean isOnRight() {
        return this.isAlignedRight;
    }
}
