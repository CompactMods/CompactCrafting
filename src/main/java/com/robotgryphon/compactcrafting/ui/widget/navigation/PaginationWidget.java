package com.robotgryphon.compactcrafting.ui.widget.navigation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.ui.widget.WidgetBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;

public class PaginationWidget extends WidgetBase implements IGuiEventListener {

    protected final ResourceLocation TEXTURE = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/widget/tabs.png");

    private Rectangle2d bounds;

    private int currentPage;
    private int pageCount;
    private Consumer<Integer> pageChangedConsumer;

    protected Rectangle2d leftArrowArea;
    protected Rectangle2d rightArrowArea;
    protected Rectangle2d leftArrowRenderArea;
    protected Rectangle2d rightArrowRenderArea;

    protected final int ARROW_OFFSET_X = 130;
    protected final int ARROW_OFFSET_Y = 0;

    protected final int ARROW_OFFSET_DEFAULT = 0;
    protected final int ARROW_OFFSET_DISABLED = 1;
    protected final int ARROW_OFFSET_HOVERED = 2;

    protected static final Vector2f ARROW_TEXTURE_SIZE = new Vector2f(10, 15);

    public PaginationWidget() {
        this.bounds = new Rectangle2d(0, 0, 0, 0);
    }

    public PaginationWidget onPageChanged(Consumer<Integer> a) {
        this.pageChangedConsumer = a;
        return this;
    }

    public static int height() {
        return (int) ARROW_TEXTURE_SIZE.y;
    }

    @Override
    public void layout(Rectangle2d parentBounds) {
        this.bounds = new Rectangle2d(
                bounds.getX(), bounds.getY(),
                parentBounds.getWidth(), (int) ARROW_TEXTURE_SIZE.y);

        layoutArrows();
    }

    private void layoutArrows() {
        this.leftArrowArea = new Rectangle2d(
                (bounds.getWidth() / 2) - (int) (ARROW_TEXTURE_SIZE.x) - 20,
                bounds.getY(),
                (int) ARROW_TEXTURE_SIZE.x,
                (int) ARROW_TEXTURE_SIZE.y
        );

        this.rightArrowArea = new Rectangle2d(
                (bounds.getWidth() / 2) + 20,
                bounds.getY(),
                (int) ARROW_TEXTURE_SIZE.x,
                (int) ARROW_TEXTURE_SIZE.y
        );

        this.leftArrowRenderArea = new Rectangle2d(
                leftArrowArea.getX(),
                0,
                leftArrowArea.getWidth(),
                leftArrowArea.getHeight()
        );

        this.rightArrowRenderArea = new Rectangle2d(
                rightArrowArea.getX(),
                0,
                rightArrowArea.getWidth(),
                rightArrowArea.getHeight()
        );
    }

    @Override
    public void setPosition(int x, int y) {
        this.bounds = new Rectangle2d(x, y, bounds.getWidth(), bounds.getHeight());
        this.layoutArrows();
    }

    private int getCurrentPage() {
        return this.currentPage;
    }

    private int getNumberPages() {
        return this.pageCount;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (leftArrowArea.contains((int) x, (int) y)) {
            this.previousPage();
            return true;
        }

        if (rightArrowArea.contains((int) x, (int) y)) {
            this.nextPage();
            return true;
        }

        return false;
    }

    private void nextPage() {
        if (this.currentPage < this.pageCount - 1) {
            currentPage++;
            pageChangedConsumer.accept(currentPage);
        }
    }

    private void previousPage() {
        if (this.currentPage > 0) {
            currentPage--;
            pageChangedConsumer.accept(currentPage);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        FontRenderer font = Minecraft.getInstance().font;

        String page = String.format("%d/%d", currentPage + 1, pageCount);
        int width = font.width(page);

        int xOffset = (bounds.getWidth() / 2) - (width / 2);
        int yOffset = (font.lineHeight / 2);


        matrixStack.pushPose();
        // matrixStack.translate(bounds.getX(),  bounds.getY(), 0);
        font.drawShadow(matrixStack,
                new StringTextComponent(page),
                xOffset, yOffset, 0xFFFFFFFF);

        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1);


        float arrowLeftU = ARROW_OFFSET_X;
        float arrowLeftV = ARROW_OFFSET_Y + ARROW_TEXTURE_SIZE.y;

        float arrowRightU = ARROW_OFFSET_X;

        boolean mouseOverAL = leftArrowArea.contains(mouseX, mouseY);
        boolean mouseOverAR = rightArrowArea.contains(mouseX, mouseY);

        if (mouseOverAL)
            arrowLeftU += (ARROW_OFFSET_HOVERED * ARROW_TEXTURE_SIZE.x);

        if (mouseOverAR)
            arrowRightU += (ARROW_OFFSET_HOVERED * ARROW_TEXTURE_SIZE.x);


        // left arrow
        if (currentPage > 0) {
            AbstractGui.blit(matrixStack,
                    leftArrowRenderArea.getX(), leftArrowRenderArea.getY(), 0,
                    arrowLeftU, arrowLeftV,
                    leftArrowRenderArea.getWidth(), leftArrowRenderArea.getHeight(),
                    256, 256);
        }

        if (currentPage + 1 < pageCount) {
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
    public Rectangle2d getBounds() {
        return this.bounds;
    }

    public void setNumberPages(int numPages) {
        this.pageCount = numPages;
    }

    public int getPageCount() {
        return this.pageCount;
    }
}
