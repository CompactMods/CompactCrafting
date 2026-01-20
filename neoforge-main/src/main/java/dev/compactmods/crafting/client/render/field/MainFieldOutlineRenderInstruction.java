package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.client.render.FacePoints;
import dev.compactmods.crafting.client.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record MainFieldOutlineRenderInstruction(AABB fieldBounds, int fieldBaseColor) implements FieldRenderInstruction {

    public static MainFieldOutlineRenderInstruction create(AABB bounds, float outlineWidth, int fieldBaseColor) {
        return new MainFieldOutlineRenderInstruction(bounds, fieldBaseColor);
    }

    @Override
    public void draw(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers) {
        int realColor = ARGB.color(0.95f, fieldBaseColor);

        VertexConsumer outline = buffers.getBuffer(RenderTypes.lines());

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion);

        FacePoints top = FacePoints.from(slightlyBiggerBecauseFoxes, Direction.UP);
        FacePoints bottom = FacePoints.from(slightlyBiggerBecauseFoxes, Direction.DOWN);

        outline.setLineWidth(1);

        top.putFacePointVertices(outline, mx, realColor);
        bottom.putFacePointVertices(outline, mx, realColor);

        // Verticals
        RenderHelper.drawLine(outline, mx, 1.0f, realColor, top.P1(), bottom.P1());
        RenderHelper.drawLine(outline, mx, 1.0f, realColor, top.P2(), bottom.P2());
        RenderHelper.drawLine(outline, mx, 1.0f, realColor, top.P3(), bottom.P3());
        RenderHelper.drawLine(outline, mx, 1.0f, realColor, top.P4(), bottom.P4());

        buffers.endBatch(RenderTypes.lines());
    }
}
