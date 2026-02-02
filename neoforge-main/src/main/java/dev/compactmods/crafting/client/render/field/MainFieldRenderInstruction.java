package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record MainFieldRenderInstruction(AABB fieldBounds, int fieldBaseColor) implements FieldRenderInstruction {
    public static MainFieldRenderInstruction create(AABB bounds, int baseColor) {
        return new MainFieldRenderInstruction(bounds, baseColor);
    }

    @Override
    public void draw(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers) {
        int realColor = ARGB.color(0.2f, fieldBaseColor);
        // TODO: Debug gizmo render in 26.1
//        HitResult hr = Minecraft.getInstance().hitResult;
//        if (hr instanceof BlockHitResult bhr && ClientUtilities.isDebugScreenOpen() && fieldProjectors.contains(bhr.getBlockPos())) {
//            doDebugFieldRender(level, mx, buffers, fieldBounds, bhr);
//        }

        VertexConsumer builder = buffers.getBuffer(CCRenderTypes.FIELD);

        double expansion = 0.005;
        AABB slightlyBiggerBecauseFoxes = fieldBounds
                .expandTowards(expansion, expansion, expansion)
                .expandTowards(-expansion, -expansion, -expansion);

        // Each projector renders its face
        // North and South projectors render the top and bottom faces
        for (var dir : Direction.values()) {
            RenderHelper.drawCubeFace(builder, mx, slightlyBiggerBecauseFoxes, realColor, dir);
        }

        buffers.endBatch(CCRenderTypes.FIELD);
    }
}
