package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class CubeRenderHelper {
    public static void addColoredVertex(VertexConsumer renderer, PoseStack stack, int color, Vec3 position) {
        renderer.addVertex(stack.last().pose(), (float) position.x(), (float) position.y(), (float) position.z())
                .setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), FastColor.ARGB32.alpha(color))
                .setNormal(stack.last(), 0, 0, 0);
    }

    public static void drawLine(VertexConsumer builder, PoseStack poseStack, int color, Vec3 start, Vec3 end)
    {
        float nX = (float) (end.x - start.x);
        float nY = (float) (end.y - start.y);
        float nZ = (float) (end.z - start.z);
        float nLen = Mth.sqrt(nX * nX + nY * nY + nZ * nZ);

        nX = nX / nLen;
        nY = nY / nLen;
        nZ = nZ / nLen;

        var pose = poseStack.last();
        builder.addVertex(pose, (float) start.x, (float) start.y, (float) start.z)
                .setColor(color)
                .setNormal(pose, nX, nY, nZ);

        builder.addVertex(pose, (float) end.x, (float) end.y, (float) end.z)
                .setColor(color)
                .setNormal(pose, nX, nY, nZ);
    }


    public static void drawCubeFace(VertexConsumer builder, PoseStack mx, AABB cube, int color, Direction face) {
        Vec3 TOP_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_LEFT);
        Vec3 TOP_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_RIGHT);
        Vec3 BOTTOM_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_LEFT);
        Vec3 BOTTOM_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_RIGHT);

        if (BOTTOM_RIGHT == Vec3.ZERO)
            return;

        Vec3i normal = Vec3i.ZERO;
        switch (face) {
            case UP -> normal = new Vec3i(0, 1, 0);
            case DOWN -> normal = new Vec3i(0, -1, 0);

            case NORTH -> normal = new Vec3i(0, 0, 1);
            case SOUTH -> normal = new Vec3i(0, 0, -1);

            case WEST -> normal = new Vec3i(1, 0, 0);
            case EAST -> normal = new Vec3i(-1, 0, 0);
        }

        Vec3i oppNormal = normal.multiply(-1);

        final int r = FastColor.ARGB32.red(color);
        final int g = FastColor.ARGB32.green(color);
        final int b = FastColor.ARGB32.blue(color);
        final int a = 50; // FastColor.ARGB32.alpha(color);

        addColoredVertex(builder, mx, color, BOTTOM_LEFT);
        addColoredVertex(builder, mx, color, BOTTOM_RIGHT);
        addColoredVertex(builder, mx, color, TOP_RIGHT);
        addColoredVertex(builder, mx, color, TOP_LEFT);
    }

    public static Vec3 getCubeFacePoint(AABB cube, Direction face, EnumCubeFaceCorner corner) {
        Vec3 BOTTOM_RIGHT = null,
                TOP_RIGHT = null,
                TOP_LEFT = null,
                BOTTOM_LEFT = null;

        switch (face) {
            case NORTH:
                BOTTOM_LEFT = new Vec3(cube.maxX, cube.minY, cube.minZ);
                BOTTOM_RIGHT = new Vec3(cube.minX, cube.minY, cube.minZ);
                TOP_LEFT = new Vec3(cube.maxX, cube.maxY, cube.minZ);
                TOP_RIGHT = new Vec3(cube.minX, cube.maxY, cube.minZ);
                break;

            case SOUTH:
                BOTTOM_RIGHT = new Vec3(cube.maxX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vec3(cube.maxX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vec3(cube.minX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vec3(cube.minX, cube.minY, cube.maxZ);
                break;

            case WEST:
                BOTTOM_RIGHT = new Vec3(cube.minX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vec3(cube.minX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vec3(cube.minX, cube.maxY, cube.minZ);
                BOTTOM_LEFT = new Vec3(cube.minX, cube.minY, cube.minZ);
                break;

            case EAST:
                BOTTOM_RIGHT = new Vec3(cube.maxX, cube.minY, cube.minZ);
                TOP_RIGHT = new Vec3(cube.maxX, cube.maxY, cube.minZ);
                TOP_LEFT = new Vec3(cube.maxX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vec3(cube.maxX, cube.minY, cube.maxZ);
                break;

            case UP:
                BOTTOM_RIGHT = new Vec3(cube.minX, cube.maxY, cube.minZ);
                TOP_RIGHT = new Vec3(cube.minX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vec3(cube.maxX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vec3(cube.maxX, cube.maxY, cube.minZ);
                break;

            case DOWN:
                BOTTOM_RIGHT = new Vec3(cube.minX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vec3(cube.minX, cube.minY, cube.minZ);
                TOP_LEFT = new Vec3(cube.maxX, cube.minY, cube.minZ);
                BOTTOM_LEFT = new Vec3(cube.maxX, cube.minY, cube.maxZ);
                break;
        }

        switch (corner) {
            case TOP_LEFT:
                return TOP_LEFT;

            case TOP_RIGHT:
                return TOP_RIGHT;

            case BOTTOM_LEFT:
                return BOTTOM_LEFT;

            case BOTTOM_RIGHT:
                return BOTTOM_RIGHT;
        }

        return Vec3.ZERO;
    }

    public static double getScanLineHeight(AABB cube, double gameTime) {
        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -RotationSpeed.MEDIUM.getSpeed()) + 1.0d) / 2) * (cube.getYsize());
        return cube.minY + zAngle;
    }

    public static Vec3 getScanLineRight(Direction face, AABB cube, double gameTime) {
        double scanHeight = getScanLineHeight(cube, gameTime);
        return switch (face) {
            case NORTH -> new Vec3(cube.minX, scanHeight, cube.minZ);
            case SOUTH -> new Vec3(cube.maxX, scanHeight, cube.maxZ);
            case WEST -> new Vec3(cube.minX, scanHeight, cube.maxZ);
            case EAST -> new Vec3(cube.maxX, scanHeight, cube.minZ);
            default -> Vec3.ZERO;
        };
    }

    public static Vec3 getScanLineLeft(Direction face, AABB cube, double gameTime) {
        double scanHeight = getScanLineHeight(cube, gameTime);
        return switch (face) {
            case NORTH -> new Vec3(cube.maxX, scanHeight, cube.minZ);
            case SOUTH -> new Vec3(cube.minX, scanHeight, cube.maxZ);
            case WEST -> new Vec3(cube.minX, scanHeight, cube.minZ);
            case EAST -> new Vec3(cube.maxX, scanHeight, cube.maxZ);
            default -> Vec3.ZERO;
        };
    }
}
