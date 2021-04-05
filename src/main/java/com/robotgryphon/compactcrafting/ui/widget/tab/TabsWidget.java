package com.robotgryphon.compactcrafting.ui.widget.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.ui.widget.IWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TabsWidget extends WidgetBase implements
        IGuiEventListener,
        IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");
    protected final int ARROW_OFFSET_X = 130;
    protected final int ARROW_OFFSET_Y = 0;

    protected final int ARROW_OFFSET_DEFAULT = 0;
    protected final int ARROW_OFFSET_DISABLED = 1;
    protected final int ARROW_OFFSET_HOVERED = 2;

    protected final Vector2f ARROW_TEXTURE_SIZE = new Vector2f(10, 15);

    protected Rectangle2d bounds;
    protected Map<Integer, GuiTab> tabs;
    private IWidgetScreen parentScreen;
    private final float parentHeight;
    private EnumTabWidgetSide screenSide;
    private GuiTab activeTab;
    private int currentPage;
    private int numPages;

    protected Rectangle2d leftArrowArea;
    protected Rectangle2d rightArrowArea;
    private Rectangle2d leftArrowRenderArea;
    private Rectangle2d rightArrowRenderArea;


    public TabsWidget(IWidgetScreen screen) {
        this.parentScreen = screen;
        this.bounds = new Rectangle2d(0, 0,
                (int) parentScreen.getScreenSize().x,
                28 + 15);

        this.tabs = new HashMap<>();
        this.screenSide = EnumTabWidgetSide.TOP;
        this.parentHeight = parentScreen.getScreenSize().y;
        this.currentPage = 0;
        this.numPages = 0;
        this.activeTab = null;
    }

    public TabsWidget addTab(GuiTab tab) {
        boolean firstAdded = tabs.isEmpty();

        this.tabs.put(tabs.size(), tab);
        Vector2f bake = getTabPosition(tab);
        tab.setPosition(bake);

        if (firstAdded)
            setActiveTab(tab);

        layout();
        return this;
    }

    public Rectangle2d getLayoutArea() {
        return this.bounds;
    }

    // TODO - Call this once during layout and cache
    protected Set<GuiTab> getCurrentScreenTabs() {
        int numTabs = getNumberTabs();
        int numTabsPerRow = bounds.getWidth() / 28;

        int start = currentPage * numTabsPerRow;
        int end = Math.min(numTabs, numTabsPerRow * (currentPage + 1));

        return IntStream.rangeClosed(start, end)
                .mapToObj(i -> tabs.get(i))
                .filter(Objects::nonNull)
                .filter(t -> !this.isActive(t))
                .collect(Collectors.toSet());
    }

    public void layout() {
        for(GuiTab tab : this.tabs.values()) {
            tab.setPosition(Vector2f.ZERO);
            if(!this.isActive(tab))
                tab.setVisible(false);
        }

        Set<GuiTab> currentScreenTabs = getCurrentScreenTabs();
        if(!currentScreenTabs.isEmpty()) {
            for (GuiTab tab : currentScreenTabs) {
                if(tab == null)
                    continue;

                Vector2f bake = getTabPosition(tab);
                tab.setPosition(bake);
                tab.setVisible(true);
            }
        }

        int numVisible = bounds.getWidth() / 28;
        this.numPages = tabs.size() / numVisible;

        // int yOffsetArrows = getArrowOffsetY();
        int parentWidth = (int) parentScreen.getScreenSize().x;

        int yOffsetArrows = 0;
        if (screenSide == EnumTabWidgetSide.BOTTOM)
            yOffsetArrows = MathHelper.floor(bounds.getHeight() - ARROW_TEXTURE_SIZE.y);

        this.leftArrowArea = new Rectangle2d(
                (parentWidth / 2) - (int) (ARROW_TEXTURE_SIZE.x) - 20,
                yOffsetArrows,
                (int) ARROW_TEXTURE_SIZE.x,
                (int) ARROW_TEXTURE_SIZE.y
        );

        this.rightArrowArea = new Rectangle2d(
                (parentWidth / 2) + 20,
                yOffsetArrows,
                (int) ARROW_TEXTURE_SIZE.x,
                (int) ARROW_TEXTURE_SIZE.y
        );

        switch (screenSide) {
            case TOP:
                leftArrowRenderArea = new Rectangle2d(
                        leftArrowArea.getX(),
                        -bounds.getHeight(),
                        leftArrowArea.getWidth(),
                        leftArrowArea.getHeight()
                );

                rightArrowRenderArea = new Rectangle2d(
                        rightArrowArea.getX(),
                        -bounds.getHeight(),
                        rightArrowArea.getWidth(),
                        rightArrowArea.getHeight()
                );

                break;

            case BOTTOM:
                this.leftArrowRenderArea = new Rectangle2d(
                        leftArrowArea.getX(),
                        leftArrowArea.getY(),
                        leftArrowArea.getWidth(),
                        leftArrowArea.getHeight()
                );

                this.rightArrowRenderArea = new Rectangle2d(
                        rightArrowArea.getX(),
                        rightArrowArea.getY(),
                        rightArrowArea.getWidth(),
                        rightArrowArea.getHeight()
                );

                break;
        }
    }

    public TabsWidget withSide(EnumTabWidgetSide side) {
        this.screenSide = side;
        return this;
    }

    protected Vector3d getRealRelativePos(double mouseX, double mouseY) {
        double realX = mouseX;
        double realY = mouseY;
        switch (screenSide) {
            case TOP:
                // Tabs on the top are offset above the widget container
                realY = mouseY + bounds.getHeight();
                break;

            case BOTTOM:
                // Tabs on the bottom are offset below the widget container
                realY = mouseY - parentHeight;
                break;
        }

        return new Vector3d(realX, realY, 0);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        Vector3d realCoords = getRealRelativePos(mouseX, mouseY);
        return bounds.contains((int) realCoords.x, (int) realCoords.y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector3d widgetCoords = getRealRelativePos(mouseX, mouseY);

        // If navigation shown, do nav checks
        if (hasNavigation()) {
            if (leftArrowArea.contains((int) widgetCoords.x, (int) widgetCoords.y)) {
                this.previousPage();
                return true;
            }

            if (rightArrowArea.contains((int) widgetCoords.x, (int) widgetCoords.y)) {
                this.nextPage();
                return true;
            }
        }

        // Do Tab Mouse Click checks
        if (screenSide == EnumTabWidgetSide.TOP) {
            widgetCoords = widgetCoords.subtract(0,
                    bounds.getHeight() - 28, 0);
        }

        Set<GuiTab> currentScreen = getCurrentScreenTabs();
        for (GuiTab tab : currentScreen) {
            if (tab.isOver(widgetCoords.x, widgetCoords.y)) {
                Vector3d tabCoords = widgetCoords.subtract(tab.screenPosition.x, tab.screenPosition.y, 0);
                tab.mouseClicked(tabCoords.x, tabCoords.y, button);
            }
        }

        return true;
    }

    private void nextPage() {
        int numVisible = bounds.getWidth() / 28;
        if (this.currentPage < this.numPages - 1) {
            currentPage++;
            this.setActiveTab(tabs.get(currentPage * numVisible));
            this.layout();
        }
    }

    private void previousPage() {
        int numVisible = bounds.getWidth() / 28;
        if (this.currentPage > 0) {
            currentPage--;
            this.setActiveTab(tabs.get(currentPage * numVisible));
            this.layout();
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.hasNavigation())
            renderPageNavigation(matrixStack, mouseX, mouseY);
    }

    public boolean hasNavigation() {
        return this.numPages > 1;
    }

    private void renderPageNavigation(MatrixStack matrixStack, int mouseX, int mouseY) {
        FontRenderer font = Minecraft.getInstance().font;

        String page = String.format("%d/%d", currentPage + 1, numPages);
        int width = font.width(page);

        int xOffset = (bounds.getWidth() / 2) - (width / 2);
        int yOffset = -bounds.getHeight() + (font.lineHeight / 2);


        matrixStack.pushPose();
        if (this.screenSide == EnumTabWidgetSide.BOTTOM) {
            matrixStack.translate(0, parentHeight, 0);
            yOffset = bounds.getHeight() - font.lineHeight - Math.round(font.lineHeight / 4.0f);
        }

        Vector3d realMouse = getRealRelativePos(mouseX, mouseY);
        font.drawShadow(matrixStack,
                new StringTextComponent(page),
                xOffset, yOffset, 0xFFFFFFFF);

        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1);


        float arrowLeftU = ARROW_OFFSET_X;
        float arrowLeftV = ARROW_OFFSET_Y + ARROW_TEXTURE_SIZE.y;

        float arrowRightU = ARROW_OFFSET_X;

        boolean mouseOverAL = leftArrowArea.contains((int) realMouse.x, (int) realMouse.y);
        boolean mouseOverAR = rightArrowArea.contains((int) realMouse.x, (int) realMouse.y);

        if (mouseOverAL)
            arrowLeftU += (ARROW_OFFSET_HOVERED * ARROW_TEXTURE_SIZE.x);

        if (mouseOverAR)
            arrowRightU += (ARROW_OFFSET_HOVERED * ARROW_TEXTURE_SIZE.x);


        // left arrow
        if(this.currentPage > 0) {
            AbstractGui.blit(matrixStack,
                    leftArrowRenderArea.getX(), leftArrowRenderArea.getY(), 0,
                    arrowLeftU, arrowLeftV,
                    leftArrowRenderArea.getWidth(), leftArrowRenderArea.getHeight(),
                    256, 256);
        }

        if(this.currentPage + 1 < numPages) {
            // right arrow
            AbstractGui.blit(matrixStack,
                    rightArrowRenderArea.getX(), rightArrowRenderArea.getY(), 0,
                    arrowRightU, ARROW_OFFSET_Y,
                    rightArrowRenderArea.getWidth(), rightArrowRenderArea.getHeight(),
                    256, 256);
        }

        matrixStack.popPose();
    }

    @Override
    public void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Set<GuiTab> currentScreen = getCurrentScreenTabs();
        if (!currentScreen.isEmpty()) {
            matrixStack.pushPose();
            if (this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            for (GuiTab tab : currentScreen)
                tab.renderPreBackground(matrixStack, mouseX, mouseY, partialTicks);

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Set<GuiTab> currentScreen = getCurrentScreenTabs();
        if (!currentScreen.isEmpty()) {
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

    public Vector2f getTabPosition(GuiTab tab) {
        int index = getTabIndex(tab);

        if (index == -1)
            return Vector2f.MIN;

        int perPage = bounds.getWidth() / 28;
        index %= perPage;

        float x = tab.getWidth() * index;
        float y = 0;

        if (tab.isOnRight())
            x = bounds.getWidth() - (tab.getWidth() * (tabs.size() - index));

        return new Vector2f(x, y);
    }

    public boolean isActive(GuiTab tab) {
        if (activeTab == null)
            return false;

        return tab == activeTab;
    }

    public void setActiveTab(GuiTab tab) {
        tab.setVisible(true);
        this.activeTab = tab;
    }
}
