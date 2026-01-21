package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

public record FacePoints(Vector3fc TOP_LEFT, Vector3fc TOP_RIGHT, Vector3fc BOTTOM_LEFT, Vector3fc BOTTOM_RIGHT) {
    public static @NonNull FacePoints from(AABB cube, Direction face) {
        final var TOP_LEFT = RenderHelper.getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_LEFT);
        final var TOP_RIGHT = RenderHelper.getCubeFacePoint(cube, face, EnumCubeFaceCorner.TOP_RIGHT);
        final var BOTTOM_LEFT = RenderHelper.getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_LEFT);
        final var BOTTOM_RIGHT = RenderHelper.getCubeFacePoint(cube, face, EnumCubeFaceCorner.BOTTOM_RIGHT);

        return new FacePoints(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT);
    }

    public void putVerticesLines(VertexConsumer builder, PoseStack mx, int color, float lineWidth) {
        // BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
        RenderHelper.drawLine(builder, mx, lineWidth, color, BOTTOM_LEFT, BOTTOM_RIGHT);
        RenderHelper.drawLine(builder, mx, lineWidth, color, TOP_LEFT, TOP_RIGHT);
        RenderHelper.drawLine(builder, mx, lineWidth, color, TOP_RIGHT, BOTTOM_RIGHT);
        RenderHelper.drawLine(builder, mx, lineWidth, color, TOP_LEFT, BOTTOM_LEFT);
    }
}
