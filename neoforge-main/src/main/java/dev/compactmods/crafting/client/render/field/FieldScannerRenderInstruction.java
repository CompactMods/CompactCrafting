package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.client.render.RenderHelper;
import dev.compactmods.crafting.client.render.projector.ProjectorRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Handles drawing the brighter "scan lines" around the main projection cube, as well as project scanner arcs.
/// These lines show visibly where the projection arcs meet the main projection cube.
public record FieldScannerRenderInstruction(AABB fieldBounds, int fieldBaseColor) implements FieldRenderInstruction {
    @Override
    public void draw(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers) {

        final var gameTime = Minecraft.getInstance()
                .getDeltaTracker()
                .getGameTimeDeltaPartialTick(false);

        VertexConsumer builder = buffers.getBuffer(RenderTypes.lines());

        mx.pushPose();

        int colorScanLine = ARGB.color(0.95f, fieldBaseColor);

        Direction.Plane.HORIZONTAL.iterator()
                .forEachRemaining(side -> {
                    final var left = ProjectorRenderHelper.getScanLineLeft(side, fieldBounds, gameTime);
                    final var right = ProjectorRenderHelper.getScanLineRight(side, fieldBounds, gameTime);

                    if(left != null && right != null) {
                        RenderHelper.drawLine(builder, mx, 1, colorScanLine, left, right);
                    }
                });

        mx.popPose();
    }
}
