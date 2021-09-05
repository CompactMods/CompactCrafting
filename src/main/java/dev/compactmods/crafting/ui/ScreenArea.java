package dev.compactmods.crafting.ui;

public class ScreenArea {

    public int x;
    public int y;
    public int width;
    public int height;

    public ScreenArea(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double mouseX, double mouseY) {
        return UiHelper.pointInBounds(mouseX, mouseY, x, y, width, height);
    }
}
