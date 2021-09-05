package dev.compactmods.crafting.util;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public class DirectionUtil {

    @Nonnull
    public static Direction.Axis getCrossDirectionAxis(Direction.Axis originalAxis) {
        switch (originalAxis) {
            case X:
                return Direction.Axis.Z;

            case Z:
                return Direction.Axis.X;
        }

        return originalAxis;
    }
}
