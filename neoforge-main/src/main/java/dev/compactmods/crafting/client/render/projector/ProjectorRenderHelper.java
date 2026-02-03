package dev.compactmods.crafting.client.render.projector;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class ProjectorRenderHelper {
    public static double getScanLineHeight(AABB cube, double gameTime) {
        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -2500) + 1.0d) / 2) * (cube.getYsize());
        return cube.minY + zAngle;
    }

    public static Vector3f getScanLineLeft(Direction face, AABB cube, double gameTime) {
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

    public static Vector3f getScanLineRight(Direction face, AABB cube, double gameTime) {
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
