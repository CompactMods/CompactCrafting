package dev.compactmods.crafting.client.ui.widget.tab;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ui.UiHelper;
import dev.compactmods.crafting.client.ui.widget.IWidgetScreen;
import dev.compactmods.crafting.client.ui.widget.WidgetBase;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TabsWidget extends WidgetBase implements
        GuiEventListener,
        IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");
    protected final int ARROW_OFFSET_X = 130;
    protected final int ARROW_OFFSET_Y = 0;

    protected final int ARROW_OFFSET_DEFAULT = 0;
    protected final int ARROW_OFFSET_DISABLED = 1;
    protected final int ARROW_OFFSET_HOVERED = 2;

    protected final Vec2 ARROW_TEXTURE_SIZE = new Vec2(10, 15);

    private final ItemRenderer itemRenderer;
    protected Map<Integer, GuiTab> tabs;
    private IWidgetScreen parentScreen;
    private final float parentHeight;
    private EnumTabWidgetSide screenSide;
    private GuiTab activeTab;
    private int currentPage;
    private int numPages;

    public TabsWidget(IWidgetScreen screen, int width, int height) {
        super(width, height);
        this.parentScreen = screen;
        this.tabs = new HashMap<>();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.screenSide = EnumTabWidgetSide.TOP;
        this.parentHeight = parentScreen.getScreenSize().y;
        this.currentPage = 0;
        this.numPages = 0;
        this.activeTab = null;
    }

    public TabsWidget addTab(GuiTab tab) {
        boolean firstAdded = tabs.isEmpty();

        this.tabs.put(tabs.size(), tab);
        Vec2 bake = getTabPosition(tab);
        tab.setPosition(bake);

        if (firstAdded)
            setActiveTab(tab);

        layout();
        return this;
    }

    public void layout() {
        for (GuiTab tab : tabs.values()) {
            Vec2 bake = getTabPosition(tab);
            tab.setPosition(bake);
        }

        int numVisible = width / 28;
        this.numPages = (tabs.size() / numVisible) + 1;
    }

    public TabsWidget withSide(EnumTabWidgetSide side) {
        this.screenSide = side;
        return this;
    }

    protected Vec3 getRealRelativePos(double mouseX, double mouseY) {
        double realX = mouseX;
        double realY = mouseY;
        switch (screenSide) {
            case TOP:
                // Tabs on the top are offset above the widget container
                realY = mouseY + 28;
                break;

            case BOTTOM:
                // Tabs on the bottom are offset below the widget container
                realY = parentHeight - mouseY + 28;
                break;
        }

        return new Vec3(realX, realY, 0);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        Vec3 realCoords = getRealRelativePos(mouseX, mouseY);
        return UiHelper.pointInBounds(realCoords.x, realCoords.y, 0, 0, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec3 realCoords = getRealRelativePos(mouseX, mouseY);
        for (GuiTab tab : tabs.values()) {
            if (tab.isOver(realCoords.x, realCoords.y)) {
                double tabX = realCoords.x - tab.screenPosition.x;
                double tabY = realCoords.y;

                tab.mouseClicked(tabX, tabY, button);
            }
        }

        return true;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;

        String page = String.format("%d/%d", currentPage + 1, numPages);
        int width = font.width(page);

        int xOffset = (this.width / 2) - (width / 2);
        int yOffset = -font.lineHeight - 28 - 2;
        int yOffsetArrows = -28 - (int) ARROW_TEXTURE_SIZE.y;

        // RenderSystem.pushMatrix();
        if (this.screenSide == EnumTabWidgetSide.BOTTOM) {
            matrixStack.translate(0, parentHeight, 0);
            yOffset = height + 4;
            yOffsetArrows = height + 1;
        }

        font.draw(matrixStack,
                new TextComponent(page),
                xOffset, yOffset, 0xFFFFFFFF);

        Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int parentWidth = (int) parentScreen.getScreenSize().x;
        int arrowLeft = (parentWidth / 2) - (int) (ARROW_TEXTURE_SIZE.x) - 20;
        int arrowRight = (parentWidth / 2) + 20;

        float arrowLeftU = ARROW_OFFSET_X;
        float arrowLeftV = ARROW_OFFSET_Y + ARROW_TEXTURE_SIZE.y;

        Vec3 realCoords = getRealRelativePos(mouseX, mouseY);

        boolean mouseOverAL = mouseX > 0 && mouseX <= 100;
        CompactCrafting.LOGGER.debug(String.format("%s,%s", mouseX, mouseY));

        // boolean mouseOverAL = UiHelper.pointInBounds(realCoords.x, realCoords.y,arrowLeft, yOffsetArrows, ARROW_TEXTURE_SIZE.x,                ARROW_TEXTURE_SIZE.y);

        if (mouseOverAL)
            arrowLeftU += (2 * ARROW_TEXTURE_SIZE.x);

        GuiComponent.blit(matrixStack,
                arrowLeft, yOffsetArrows, 0,
                arrowLeftU, arrowLeftV,
                (int) ARROW_TEXTURE_SIZE.x, (int) ARROW_TEXTURE_SIZE.y,
                256, 256);

        GuiComponent.blit(matrixStack,
                arrowRight, yOffsetArrows, 0,
                ARROW_OFFSET_X, ARROW_OFFSET_Y,
                (int) ARROW_TEXTURE_SIZE.x, (int) ARROW_TEXTURE_SIZE.y,
                256, 256);

        // RenderSystem.popMatrix();
    }

    @Override
    public void renderPreBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.tabs.isEmpty()) {
            int numTabs = getNumberTabs();
            int numTabsPerRow = width / 28;

            int start = currentPage * numTabs;
            int end = Math.min(numTabs, numTabsPerRow * (currentPage + 1));

            matrixStack.pushPose();
            if (this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            for (int idx = start; idx < end; idx++) {
                GuiTab tab = this.tabs.get(idx);

                // Check tab active state - active tab needs to be drawn in post
                if (!isActive(tab))
                    tab.renderPreBackground(matrixStack, mouseX, mouseY, partialTicks);
            }

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPostBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.tabs.isEmpty()) {
            matrixStack.pushPose();
            if (this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            if (activeTab != null)
                activeTab.renderPostBackground(matrixStack, mouseX, mouseY, partialTicks);

            matrixStack.popPose();
        }
    }

    public int getNumberTabs() {
        return tabs.size();
    }

    public EnumTabWidgetSide getSide() {
        return this.screenSide;
    }

    protected int getTabIndex(GuiTab tab) {
        for (Map.Entry<Integer, GuiTab> t : tabs.entrySet()) {
            if (t.getValue() == tab)
                return t.getKey();
        }

        return -1;
    }

    public Vec2 getTabPosition(GuiTab tab) {
        int index = getTabIndex(tab);
        if (index == -1)
            return Vec2.MIN;

        if (!tab.isOnRight())
            return new Vec2(tab.getWidth() * index, 0);

        float x = this.width - (tab.getWidth() * (tabs.size() - index));
        return new Vec2(x, 0);
    }

    public boolean isActive(GuiTab tab) {
        if (activeTab == null)
            return false;

        return tab == activeTab;
    }

    public void setActiveTab(GuiTab tab) {
        this.activeTab = tab;
    }
}
