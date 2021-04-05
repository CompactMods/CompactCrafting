package com.robotgryphon.compactcrafting.ui.widget.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.ui.widget.IWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import com.robotgryphon.compactcrafting.ui.widget.navigation.PaginationWidget;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TabsWidget extends WidgetBase implements
        IGuiEventListener,
        IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");

    protected PaginationWidget pages;

    protected Rectangle2d bounds;
    protected List<GuiTab> tabs;
    private IWidgetScreen parentScreen;
    private final float parentHeight;
    private EnumTabWidgetSide screenSide;
    private GuiTab activeTab;

    private int currentPage;


    protected Set<GuiTab> currentScreenTabs;
    private Consumer<GuiTab> changeListener;

    public TabsWidget(IWidgetScreen screen) {
        this.parentScreen = screen;

        this.tabs = new ArrayList<>();
        this.currentScreenTabs = Collections.emptySet();

        this.screenSide = EnumTabWidgetSide.TOP;
        this.parentHeight = parentScreen.getScreenSize().y;
        this.currentPage = 0;
        this.activeTab = null;

        this.pages = new PaginationWidget()
                .onPageChanged((page) -> {
                    this.currentPage = page;
                    this.setActiveTab(tabs.get(currentPage * this.getNumVisibleTabs()));
                    this.layoutTabs();
                });

        this.layout(screen.getBounds());
    }

    public TabsWidget addTab(GuiTab tab) {
        boolean firstAdded = tabs.isEmpty();
        if (firstAdded)
            setActiveTab(tab);

        this.tabs.add(tab);
        this.layoutTabs();
        return this;
    }

    public TabsWidget addTabs(List<GuiTab> tabs) {
        if (tabs.isEmpty())
            return this;

        if (this.tabs.isEmpty()) {
            GuiTab first = tabs.get(0);
            setActiveTab(first);
        }

        this.tabs.addAll(tabs);
        this.layoutTabs();
        return this;
    }

    @Override
    public Rectangle2d getBounds() {
        return this.bounds;
    }

    protected int getNumVisibleTabs() {
        return bounds.getWidth() / 28;
    }

    protected Set<GuiTab> getCurrentScreenTabs() {
        int numTabs = getNumberTabs();
        if(numTabs == 0)
            return Collections.emptySet();

        int numVisible = getNumVisibleTabs();
        int start = currentPage * numVisible;
        int end = Math.min(numTabs - 1, start + numVisible - 1);

        return IntStream.rangeClosed(start, end)
                .mapToObj(i -> tabs.get(i))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void layout(Rectangle2d parentBounds) {
        this.bounds = new Rectangle2d(0, 0,
                parentScreen.getBounds().getWidth(),
                28 + 15);

        int paginationWidgetY = 0;
        if (screenSide == EnumTabWidgetSide.BOTTOM)
            paginationWidgetY = MathHelper.floor(bounds.getHeight() - PaginationWidget.height());

        pages.layout(this.bounds);

        int px = pages.getBounds().getX();
        pages.setPosition(px, paginationWidgetY);
    }

    private void layoutTabs() {
        double numVisible = Math.floorDiv(bounds.getWidth(), 28);
        int numPages = MathHelper.ceil(tabs.size() / numVisible);
        pages.setNumberPages(numPages);

        for (GuiTab tab : this.tabs) {
            tab.setPosition(Vector2f.MIN);
            tab.setVisible(false);
        }

        this.currentScreenTabs = getCurrentScreenTabs();
        if (!currentScreenTabs.isEmpty()) {
            for (GuiTab tab : currentScreenTabs) {
                if (tab == null)
                    continue;

                Vector2f bake = getTabPosition(tab);
                tab.setPosition(bake);
                tab.setVisible(true);
            }
        }
    }

    @Override
    public void setPosition(int x, int y) {
        this.bounds = new Rectangle2d(x, y, bounds.getWidth(), bounds.getHeight());
    }

    public TabsWidget withSide(EnumTabWidgetSide side) {
        this.screenSide = side;
        this.layout(parentScreen.getBounds());
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
            boolean pageHandled = pages.mouseClicked(widgetCoords.x, widgetCoords.y, button);
            if(pageHandled)
                return true;
        }

        // Do Tab Mouse Click checks
        if (screenSide == EnumTabWidgetSide.TOP) {
            widgetCoords = widgetCoords.subtract(0,
                    bounds.getHeight() - 28, 0);
        }

        for (GuiTab tab : this.currentScreenTabs) {
            if (tab.isOver(widgetCoords.x, widgetCoords.y)) {
                Vector3d tabCoords = widgetCoords.subtract(tab.screenPosition.x, tab.screenPosition.y, 0);
                boolean handled = tab.mouseClicked(tabCoords.x, tabCoords.y, button);
                if(handled)
                    return true;
            }
        }

        return false;
    }

    public TabsWidget onTabChanged(Consumer<GuiTab> listener) {
        this.changeListener = listener;
        return this;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Vector3d realMouse = getRealRelativePos(mouseX, mouseY);
        if (this.hasNavigation()) {
            matrixStack.pushPose();
            switch(screenSide) {
                case TOP:
                    matrixStack.translate(0, -bounds.getHeight(), 0);
                    break;

                case BOTTOM:
                    matrixStack.translate(
                            0,
                            (parentHeight + bounds.getHeight()) - pages.getBounds().getHeight(),
                            0
                    );
                    break;
            }

            pages.render(matrixStack, (int) realMouse.x, (int) realMouse.y, partialTicks);
            matrixStack.popPose();
        }
    }

    public boolean hasNavigation() {
        return this.pages.getPageCount() > 1;
    }

    @Override
    public void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.currentScreenTabs.isEmpty()) {
            matrixStack.pushPose();
            if (this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            for (GuiTab tab : currentScreenTabs)
                tab.renderPreBackground(matrixStack, mouseX, mouseY, partialTicks);

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!currentScreenTabs.isEmpty()) {
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

    public Vector2f getTabPosition(GuiTab tab) {
        int index = tabs.indexOf(tab);

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
        if(this.changeListener != null)
            changeListener.accept(tab);
    }
}
