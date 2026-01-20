package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class RenderHelper {

    public static void addColoredVertex(VertexConsumer renderer, PoseStack stack, int color, Vector3fc position) {
        renderer.addVertex(stack.last().pose(), position.x(), position.y(), position.z())
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(stack.last(), 0, 0, 0);
    }

    public static void addColoredVertex(VertexConsumer renderer, PoseStack stack, int color, Vector3fc position, float lineWidth) {
        renderer.addVertex(stack.last().pose(), position.x(), position.y(), position.z())
                .setLineWidth(lineWidth)
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(stack.last(), 0, 0, 0);
    }

    public static void drawLine(VertexConsumer builder, PoseStack poseStack, float width, int color, Vector3fc start, Vector3fc end) {

    }

    public static void drawLine(VertexConsumer builder, PoseStack poseStack, float width, int color, Vector3dc start, Vector3dc end)
    {
        Vector3d n = new Vector3d();
        end.sub(start, n);

        final var nLen = Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
        n.div(nLen);

        var pose = poseStack.last();

        builder.addVertex(pose, new Vector3f(start))
                .setLineWidth(width)
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(pose, new Vector3f(n));

        builder.addVertex(pose, new Vector3f(end))
                .setLineWidth(width)
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(pose, new Vector3f(n));
    }

    public static void drawCubeFace(VertexConsumer builder, PoseStack mx, AABB cube, int color, Direction face) {
        final var TOP_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_LEFT);
        final var TOP_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_RIGHT);
        final var BOTTOM_LEFT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_LEFT);
        final var BOTTOM_RIGHT = getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_RIGHT);

        addColoredVertex(builder, mx, color, BOTTOM_LEFT);
        addColoredVertex(builder, mx, color, BOTTOM_RIGHT);
        addColoredVertex(builder, mx, color, TOP_RIGHT);
        addColoredVertex(builder, mx, color, TOP_LEFT);
    }

    public static Vector3fc getCubeFacePoint(AABB cube, Direction face, EnumCubeFaceCorner corner) {
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

        return switch (corner) {
            case TOP_LEFT -> new Vector3f(TOP_LEFT);
            case TOP_RIGHT -> new Vector3f(TOP_RIGHT);
            case BOTTOM_LEFT -> new Vector3f(BOTTOM_LEFT);
            case BOTTOM_RIGHT -> new Vector3f(BOTTOM_RIGHT);
        };
    }
}
