package dev.compactmods.crafting.client.ui.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.client.ui.container.TestContainer;
import dev.compactmods.crafting.client.ui.widget.ContainerWidgetScreen;
import dev.compactmods.crafting.client.ui.widget.IWidgetScreen;
import dev.compactmods.crafting.client.ui.widget.WidgetBase;
import dev.compactmods.crafting.client.ui.widget.WidgetHolder;
import dev.compactmods.crafting.client.ui.widget.tab.EnumTabWidgetSide;
import dev.compactmods.crafting.client.ui.widget.tab.GuiTab;
import dev.compactmods.crafting.client.ui.widget.tab.TabsWidget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TestScreen extends ContainerWidgetScreen<TestContainer> implements IWidgetScreen {

    private ResourceLocation GUI = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/edit-screen.png");
    private final Player player;

    public TestScreen(TestContainer c, Inventory inv, Component name) {
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
                    player.displayClientMessage(new TextComponent("hi!"), true);
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
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.render(ms, mouseX, mouseY, partialTicks);
        ms.popPose();
    }

    @Override
    protected void renderLabels(PoseStack ms, int mouseX, int mouseY) {
        // this.font.draw(ms, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(ms, this.player.getInventory().getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        ms.pushPose();
        ms.translate(leftPos, topPos, 0);
        this.widgets.renderPreBackground(ms, mouseX, mouseY, partialTicks);
        ms.popPose();

        this.minecraft.getTextureManager().bindForSetup(GUI);
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
    public Vec2 getScreenSize() {
        return new Vec2(this.imageWidth, this.imageHeight);
    }
}
