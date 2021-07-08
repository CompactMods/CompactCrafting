package com.robotgryphon.compactcrafting.projector.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public abstract class CubeRenderHelper {
    public static void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, int color, Vector3d position) {
        renderer.vertex(stack.last().pose(), (float) position.x(), (float) position.y(), (float) position.z())
                .color(ColorHelper.PackedColor.red(color), ColorHelper.PackedColor.green(color), ColorHelper.PackedColor.blue(color), ColorHelper.PackedColor.alpha(color))
                .endVertex();
    }

    public static void drawCubeFace(IVertexBuilder builder, MatrixStack mx, AxisAlignedBB cube, int color, Direction face) {
        Vector3d TOP_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_LEFT);
        Vector3d TOP_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_RIGHT);
        Vector3d BOTTOM_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_LEFT);
        Vector3d BOTTOM_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_RIGHT);

        if (BOTTOM_RIGHT == Vector3d.ZERO)
            return;

        addColoredVertex(builder, mx, color, BOTTOM_LEFT);
        addColoredVertex(builder, mx, color, BOTTOM_RIGHT);
        addColoredVertex(builder, mx, color, TOP_RIGHT);
        addColoredVertex(builder, mx, color, TOP_LEFT);
    }

    public static Vector3d getCubeFacePoint(AxisAlignedBB cube, Direction face, EnumCubeFaceCorner corner) {
        Vector3d BOTTOM_RIGHT = null,
                TOP_RIGHT = null,
                TOP_LEFT = null,
                BOTTOM_LEFT = null;

        switch (face) {
            case NORTH:
                BOTTOM_LEFT = new Vector3d(cube.maxX, cube.minY, cube.minZ);
                BOTTOM_RIGHT = new Vector3d(cube.minX, cube.minY, cube.minZ);
                TOP_LEFT = new Vector3d(cube.maxX, cube.maxY, cube.minZ);
                TOP_RIGHT = new Vector3d(cube.minX, cube.maxY, cube.minZ);
                break;

            case SOUTH:
                BOTTOM_RIGHT = new Vector3d(cube.maxX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vector3d(cube.maxX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vector3d(cube.minX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vector3d(cube.minX, cube.minY, cube.maxZ);
                break;

            case WEST:
                BOTTOM_RIGHT = new Vector3d(cube.minX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vector3d(cube.minX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vector3d(cube.minX, cube.maxY, cube.minZ);
                BOTTOM_LEFT = new Vector3d(cube.minX, cube.minY, cube.minZ);
                break;

            case EAST:
                BOTTOM_RIGHT = new Vector3d(cube.maxX, cube.minY, cube.minZ);
                TOP_RIGHT = new Vector3d(cube.maxX, cube.maxY, cube.minZ);
                TOP_LEFT = new Vector3d(cube.maxX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vector3d(cube.maxX, cube.minY, cube.maxZ);
                break;

            case UP:
                BOTTOM_RIGHT = new Vector3d(cube.minX, cube.maxY, cube.minZ);
                TOP_RIGHT = new Vector3d(cube.minX, cube.maxY, cube.maxZ);
                TOP_LEFT = new Vector3d(cube.maxX, cube.maxY, cube.maxZ);
                BOTTOM_LEFT = new Vector3d(cube.maxX, cube.maxY, cube.minZ);
                break;

            case DOWN:
                BOTTOM_RIGHT = new Vector3d(cube.minX, cube.minY, cube.maxZ);
                TOP_RIGHT = new Vector3d(cube.minX, cube.minY, cube.minZ);
                TOP_LEFT = new Vector3d(cube.maxX, cube.minY, cube.minZ);
                BOTTOM_LEFT = new Vector3d(cube.maxX, cube.minY, cube.maxZ);
                break;
        }

        switch(corner) {
            case TOP_LEFT:
                return TOP_LEFT;

            case TOP_RIGHT:
                return TOP_RIGHT;

            case BOTTOM_LEFT:
                return BOTTOM_LEFT;

            case BOTTOM_RIGHT:
                return BOTTOM_RIGHT;
        }

        return Vector3d.ZERO;
    }

    public static double getScanLineHeight(AxisAlignedBB cube, double gameTime) {
        // Get the height of the scan line
        double zAngle = ((Math.sin(Math.toDegrees(gameTime) / -FieldProjectorRenderer.RotationSpeed.MEDIUM.getSpeed()) + 1.0d) / 2) * (cube.getYsize());
        double scanHeight = (cube.minY + zAngle);

        return scanHeight;
    }

    public static Vector3d getScanLineRight(Direction face, AxisAlignedBB cube, double gameTime) {
        double scanHeight = getScanLineHeight(cube, gameTime);
        switch (face) {
            case NORTH:
                return new Vector3d(cube.minX, scanHeight, cube.minZ);

            case SOUTH:
                return new Vector3d(cube.maxX, scanHeight, cube.maxZ);

            case WEST:
                return new Vector3d(cube.minX, scanHeight, cube.maxZ);

            case EAST:
                return new Vector3d(cube.maxX, scanHeight, cube.minZ);
        }

        return Vector3d.ZERO;
    }

    public static Vector3d getScanLineLeft(Direction face, AxisAlignedBB cube, double gameTime) {
        double scanHeight = getScanLineHeight(cube, gameTime);
        switch (face) {
            case NORTH:
                return new Vector3d(cube.maxX, scanHeight, cube.minZ);

            case SOUTH:
                return new Vector3d(cube.minX, scanHeight, cube.maxZ);

            case WEST:
                return new Vector3d(cube.minX, scanHeight, cube.minZ);

            case EAST:
                return new Vector3d(cube.maxX, scanHeight, cube.maxZ);
        }

        return Vector3d.ZERO;
    }

}
