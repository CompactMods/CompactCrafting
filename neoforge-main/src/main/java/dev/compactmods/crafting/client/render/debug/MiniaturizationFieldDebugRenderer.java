package dev.compactmods.crafting.client.render.debug;

import dev.compactmods.crafting.field.impl.MiniaturizationField;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.Vec3;

// TODO: Debug Rendering
public record MiniaturizationFieldDebugRenderer(MiniaturizationField field) implements Gizmo {

//    private static void doDebugFieldRender(Level level, PoseStack mx, MultiBufferSource.BufferSource buffers, AABB fieldBounds, BlockHitResult bhr) {
//        var state = level.getBlockState(bhr.getBlockPos());
//        if (!state.is(CCBlocks.FIELD_PROJECTOR_BLOCK.get()))
//            return;
//
//        final var hoveringProjector = FieldProjectorBlock.getDirection(level, bhr.getBlockPos())
//                .orElse(Direction.DOWN)
//                .getOpposite();
//
//        VertexConsumer lineBuilder = buffers.getBuffer(RenderTypes.lines());
//
//        Vec3 hoveredProjectorPos = Vec3.atCenterOf(bhr.getBlockPos());
//        Vec3 debugOrigin = new Vec3(.5, .5, .5)
//                .add(hoveredProjectorPos);
//
//        Vec3 bottomLeft = RenderHelper
//                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.BOTTOM_LEFT);
//
//        Vec3 bottomRight = RenderHelper
//                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.BOTTOM_RIGHT);
//
//        Vec3 topLeft = RenderHelper
//                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.TOP_LEFT);
//
//        Vec3 topRight = RenderHelper
//                .getCubeFacePoint(fieldBounds, hoveringProjector, EnumCubeFaceCorner.TOP_RIGHT);
//
//        mx.pushPose();
//        {
//            RenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, bottomLeft);
//            RenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, bottomRight);
//            RenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, topRight);
//            RenderHelper.drawLine(lineBuilder, mx, 0xFFFF0000, debugOrigin, topLeft);
//        }
//        mx.popPose();
//
//        buffers.endBatch(RenderTypes.lines());
//    }

    @Override
    public void emit(GizmoPrimitives primitives, float alphaMultiplier) {
        primitives.addText(Vec3.atCenterOf(field.location().centerBlock()),
                field.location().center().toString(),
                TextGizmo.Style.whiteAndCentered());
    }
}
