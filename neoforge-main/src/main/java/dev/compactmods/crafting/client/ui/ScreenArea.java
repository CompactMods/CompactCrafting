package dev.compactmods.crafting.client.ui;

public class ScreenArea {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public ScreenArea(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public int getRight() {
        return x + width;
    }

    public int getBottom() {
        return y + height;
    }
}