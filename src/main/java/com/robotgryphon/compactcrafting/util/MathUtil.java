package com.robotgryphon.compactcrafting.util;

public class MathUtil {
    public static double calculateScale(double progress, double requiredTime) {
        double waveDensity = 0.3d;
        double h = 0.2d;
        double p = 1 - (progress / requiredTime);
        double l = 2 * Math.PI / 0.5d;
        double n = Math.floor(requiredTime / l) + 0.5d;

        // q(progress) = w(x)
        double q = 0.5 * Math.cos(waveDensity * ((l * n * progress) / requiredTime)) + 0.5;

        double scale = p - (h * p) + (h * q);
        return scale;
    }
}
