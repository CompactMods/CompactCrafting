package dev.compactmods.crafting.client.render.projector;

import dev.compactmods.crafting.client.render.RotationSpeed;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public abstract class ProjectorRenderHelper {
    public static double getScanLineHeight(AABB cube, double gameTime) {
        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -RotationSpeed.MEDIUM.getSpeed()) + 1.0d) / 2) * (cube.getYsize());
        return cube.minY + zAngle;
    }

    @Nullable
    public static Vector3fc getScanLineLeft(Direction face, AABB cube, double gameTime) {
        if(face.getAxis().isVertical())
            return null;

        double scanHeight = getScanLineHeight(cube, gameTime);
        final var d = switch (face) {
            case NORTH -> new Vector3d(cube.maxX, scanHeight, cube.minZ);
            case SOUTH -> new Vector3d(cube.minX, scanHeight, cube.maxZ);
            case WEST -> new Vector3d(cube.minX, scanHeight, cube.minZ);
            case EAST -> new Vector3d(cube.maxX, scanHeight, cube.maxZ);
            default -> throw new IllegalStateException("Unexpected value: " + face);
        };

        return new Vector3f(d);
    }

    @Nullable
    public static Vector3fc getScanLineRight(Direction face, AABB cube, double gameTime) {
        if(face.getAxis().isVertical())
            return null;

        double scanHeight = getScanLineHeight(cube, gameTime);
        final var d = switch (face) {
            case NORTH -> new Vector3d(cube.minX, scanHeight, cube.minZ);
            case SOUTH -> new Vector3d(cube.maxX, scanHeight, cube.maxZ);
            case WEST -> new Vector3d(cube.minX, scanHeight, cube.maxZ);
            case EAST -> new Vector3d(cube.maxX, scanHeight, cube.minZ);
            default -> throw new IllegalStateException("Unexpected value: " + face);
        };

        return new Vector3f(d);
    }
}
