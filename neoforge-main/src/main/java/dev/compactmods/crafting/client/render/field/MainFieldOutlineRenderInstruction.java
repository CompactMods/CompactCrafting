package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.FacePoints;
import dev.compactmods.crafting.client.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

public record MainFieldOutlineRenderInstruction(AABB fieldBounds, int fieldBaseColor) implements FieldRenderInstruction {

    public static MainFieldOutlineRenderInstruction create(AABB bounds, float outlineWidth, int fieldBaseColor) {
        return new MainFieldOutlineRenderInstruction(bounds, fieldBaseColor);
    }

    @Override
    public void draw(PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        int realColor = ARGB.color(0.95f, fieldBaseColor);

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion);

        FacePoints top = FacePoints.from(slightlyBiggerBecauseFoxes, Direction.UP);
        FacePoints bottom = FacePoints.from(slightlyBiggerBecauseFoxes, Direction.DOWN);

        VertexConsumer fieldOutline = buffers.getBuffer(CCRenderTypes.FIELD_OUTLINE);
        top.putVerticesLines(fieldOutline, poseStack, realColor, 2.0f);
        bottom.putVerticesLines(fieldOutline, poseStack, realColor, 2.0f);

        // Verticals
        RenderHelper.drawLine(fieldOutline, poseStack, 2.0f, realColor, top.BOTTOM_LEFT(), bottom.TOP_LEFT());
        RenderHelper.drawLine(fieldOutline, poseStack, 2.0f, realColor, top.BOTTOM_RIGHT(), bottom.TOP_RIGHT());
        RenderHelper.drawLine(fieldOutline, poseStack, 2.0f, realColor, top.TOP_LEFT(), bottom.BOTTOM_LEFT());
        RenderHelper.drawLine(fieldOutline, poseStack, 2.0f, realColor, top.TOP_RIGHT(), bottom.BOTTOM_RIGHT());
        buffers.endBatch(CCRenderTypes.FIELD_OUTLINE);
    }
}
