package dev.compactmods.crafting.util;

import dev.compactmods.crafting.api.field.MiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;

public class MathUtil {
    public static double calculateFieldScale(MiniaturizationField field) {
        double requiredTime = field.getCurrentRecipe().map(IMiniaturizationRecipe::getCraftingTime).orElse(1);
        return calculateFieldScale(field.getProgress(), requiredTime);
    }

    public static double calculateFieldScale(double progress, double requiredTime) {
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
