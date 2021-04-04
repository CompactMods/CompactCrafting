package com.robotgryphon.compactcrafting.ui.widget.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.ui.UiHelper;
import com.robotgryphon.compactcrafting.ui.widget.IWidgetScreen;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import com.robotgryphon.compactcrafting.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class GenericTabsWidget extends WidgetBase implements
        IGuiEventListener,
        IWidgetPreBackgroundRenderable, IWidgetPostBackgroundRenderable {
    private final ItemRenderer itemRenderer;
    protected Map<Integer, GenericTab> tabs;
    private IWidgetScreen parentScreen;
    private final float parentHeight;
    private EnumTabWidgetSide screenSide;
    private GenericTab activeTab;

    public GenericTabsWidget(IWidgetScreen screen, int width, int height) {
        super(width, height);
        this.parentScreen = screen;
        this.tabs = new HashMap<>();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.screenSide = EnumTabWidgetSide.TOP;
        this.parentHeight = parentScreen.getScreenSize().y;

        this.tabs.put(0, new GenericTab(this, new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get())));
        this.tabs.put(1, new GenericTab(this, new ItemStack(Items.COAL)));
        this.tabs.put(2, new GenericTab(this, new ItemStack(Items.REDSTONE)).onRight());

        this.activeTab = tabs.get(0);

        for(GenericTab tab : tabs.values()) {
            Vector2f bake = getTabPosition(tab);
            tab.setPosition(bake);
        }
    }

    public GenericTabsWidget withSide(EnumTabWidgetSide side) {
        this.screenSide = side;
        return this;
    }

    protected Vector3d getRealRelativePos(double mouseX, double mouseY) {
        double realX = mouseX;
        double realY = mouseY;
        switch(screenSide) {
            case TOP:
                // Tabs on the top are offset above the widget container
                realY = mouseY + 28;
                break;

            case BOTTOM:
                // Tabs on the bottom are offset below the widget container
                realY = parentHeight - mouseY + 28;
                break;
        }

        return new Vector3d(realX, realY, 0);
    }
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        Vector3d realCoords = getRealRelativePos(mouseX, mouseY);
        return UiHelper.pointInBounds(realCoords.x, realCoords.y, 0, 0, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector3d realCoords = getRealRelativePos(mouseX, mouseY);
        for(GenericTab tab : tabs.values())
        {
            if(tab.isOver(realCoords.x, realCoords.y)) {
                double tabX = realCoords.x - tab.screenPosition.x;
                double tabY = realCoords.y;

                tab.mouseClicked(tabX, tabY, button);
            }
        }

        return true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void renderPreBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(!this.tabs.isEmpty()) {
            int tabPage = 0;

            int numTabs = getNumberTabs();

            int start = tabPage * numTabs;
            int end = Math.min(numTabs, ((tabPage + 1) * numTabs));

            matrixStack.pushPose();
            if(this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            for (int idx = start; idx < end; idx++) {
                GenericTab tab = this.tabs.get(idx);

                // Check tab active state - active tab needs to be drawn in post
                if(!isActive(tab))
                    tab.renderPreBackground(matrixStack, mouseX, mouseY, partialTicks);
            }

            matrixStack.popPose();
        }
    }

    @Override
    public void renderPostBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(!this.tabs.isEmpty()) {
            matrixStack.pushPose();
            if(this.screenSide == EnumTabWidgetSide.BOTTOM)
                matrixStack.translate(0, parentHeight, 0);

            if(activeTab != null)
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

    protected int getTabIndex(GenericTab tab) {
        for(Map.Entry<Integer, GenericTab> t : tabs.entrySet()) {
            if(t.getValue() == tab)
                return t.getKey();
        }

        return -1;
    }

    public Vector2f getTabPosition(GenericTab tab) {
        int index = getTabIndex(tab);
        if(index == -1)
            return Vector2f.MIN;

        if(!tab.isOnRight())
            return new Vector2f(tab.getWidth() * index, 0);

        float x = this.width - (tab.getWidth() * (tabs.size() - index));
        return new Vector2f(x, 0);
    }

    public boolean isActive(GenericTab tab) {
        if(activeTab == null)
            return false;

        return tab == activeTab;
    }

    public void setActiveTab(GenericTab tab) {
        this.activeTab = tab;
    }
}
