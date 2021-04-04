package com.robotgryphon.compactcrafting.ui;

public abstract class UiHelper {

    public static boolean pointInBounds(double px, double py, double x, double y, double width, double height) {
        boolean top = px >= x;
        boolean left = py >= y;
        boolean right = px <= x + width;
        boolean bottom = py <= y + height;

        return top && left && bottom && right;
    }


}
