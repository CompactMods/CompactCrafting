package dev.compactmods.crafting.client.render.projector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.compactmods.crafting.client.render.RotationSpeed;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FieldProjectorRenderer implements BlockEntityRenderer<FieldProjectorEntity, FieldProjectorRenderState> {

//    public static final ModelIdentifier FIELD_DISH_RL = ModelIdentifier.standalone(CompactCrafting.modRL("block/field_projector_dish"));

//    private BakedModel bakedModelCached;

    public FieldProjectorRenderer(BlockEntityRendererProvider.Context ctx) {

    }

//    private BakedModel getModel() {
//        if (bakedModelCached == null) {
//            ModelManager models = Minecraft.getInstance()
//                    .getBlockRenderer()
//                    .getBlockModelShaper()
//                    .getModelManager();
//
//            bakedModelCached = models.getModel(FIELD_DISH_RL);
//        }
//
//        return bakedModelCached;
//    }
//
//    @Override
//    public boolean shouldRenderOffScreen(FieldProjectorEntity te) {
//        return true;
//    }
//
//    @Override
//    public AABB getRenderBoundingBox(FieldProjectorEntity blockEntity) {
//        final var p = blockEntity.getBlockPos();
//        return AABB.encapsulatingFullBlocks(p, p).inflate(20);
//    }

    @Override
    public FieldProjectorRenderState createRenderState() {
        return new FieldProjectorRenderState();
    }

    @Override
    public void extractRenderState(FieldProjectorEntity blockEntity, FieldProjectorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        state.gameTime = blockEntity.getLevel().getGameTime();
    }

    @Override
    public void submit(FieldProjectorRenderState renderState, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        VertexConsumer cutoutBlocks = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(Sheets.cutoutBlockSheet());
//        BakedModel baked = this.getModel();

        poseStack.pushPose();
        {
            poseStack.translate(.5, 0, .5);

            double yaw = Math.sin(Math.toDegrees(renderState.gameTime) / RotationSpeed.MEDIUM.getSpeed()) * 10;

            Direction facing = renderState.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (facing != Direction.WEST) {
                float angle = facing.toYRot() - 90;
                poseStack.mulPose(Axis.YN.rotationDegrees(angle));
            }

            float yDiskOffset = -0.66f;
            poseStack.translate(0.0, -yDiskOffset, 0.0);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) yaw));
            poseStack.translate(0.0, yDiskOffset, 0.0);

            poseStack.translate(-.5, 0, -.5);

            // TODO - Revisit render types
//            blockRenderer.getModelRenderer()
//                    .renderModel(poseStack.last(), cutoutBlocks, renderState.currentState,
//                            baked,
//                            red,
//                            green,
//                            blue,
//                            combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
        }
        poseStack.popPose();
    }
}
