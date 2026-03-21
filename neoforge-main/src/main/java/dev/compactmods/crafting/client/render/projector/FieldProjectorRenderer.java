package dev.compactmods.crafting.client.render.projector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.world.ProjectorBlock;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.CompactCraftingClient;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.projector.model.FieldProjectorDishModel;
import dev.compactmods.crafting.projector.model.ProjectorDishRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class FieldProjectorRenderer implements BlockEntityRenderer<FieldProjectorEntity, FieldProjectorRenderState> {

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockStateModel model;
    private final BlockModelResolver blockModelResolver;

    public FieldProjectorRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockModelResolver = ctx.blockModelResolver();
        final var manager = Minecraft.getInstance()
                .getModelManager();

        model = manager.getStandaloneModel(CompactCraftingClient.PROJECTOR_DISH_MODEL_KEY);
    }

    @Override
    public FieldProjectorRenderState createRenderState() {
        return new FieldProjectorRenderState();
    }

    @Override
    public void extractRenderState(FieldProjectorEntity blockEntity, FieldProjectorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

        final var currBlockState = blockEntity.getBlockState();
        final var level = blockEntity.getLevel();

        state.facing = ProjectorBlock.facing(currBlockState);
        state.gameTime = level.getGameTime();
        state.projectorColor = ARGB.opaque(ClientConfig.projectorColor);

        final var size = ProjectorBlock.fieldSize(currBlockState);
        final var projectorGlobalPos = GlobalPos.of(blockEntity.getLevel().dimension(), state.blockPos);
        final var projectorPlacement = ProjectorBlock.placement(projectorGlobalPos, currBlockState);
        state.fieldLocation = MiniaturizationFieldLocation.compute(
                projectorGlobalPos.dimension(), size, projectorPlacement
        );

        if(level instanceof ClientLevel cl) {
            var parts = new ArrayList<BlockStateModelPart>();
            model.collectParts(cl, projectorGlobalPos.pos(), currBlockState,
                    level.getRandom(), parts);

            state.dishModelParts = parts;
        }
    }

    @Override
    public void submit(FieldProjectorRenderState renderState, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {

        final var yDiskOffset = -0.66f;

        var yaw = Math.sin(Math.toDegrees(renderState.gameTime) / 2500) * 10;
        var angle = renderState.facing.toYRot();
        if (renderState.facing.getAxis().equals(Direction.NORTH.getAxis()))
            angle += 180;

        poseStack.pushPose();
        {
            poseStack.translate(.5, 0, .5);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(0.0, -yDiskOffset, 0.0);
            poseStack.mulPose(Axis.XN.rotationDegrees((float) yaw));
            poseStack.translate(0.0, yDiskOffset, 0.0);
            poseStack.translate(-.5, 0, -.5);

            submitNodeCollector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(),
                    renderState.dishModelParts,
                    new int[] { 0, 1 },
                    renderState.lightCoords, OverlayTexture.NO_OVERLAY,
                    0);
        }
        poseStack.popPose();

        submitNodeCollector.submitCustomGeometry(poseStack,
                CCRenderTypes.FIELD,
                new ScanArcGeometry(renderState));
    }

    // Handles drawing the brighter "scan line" around the main projection cube. These lines show visibly
    // where the projection arcs meet the main projection cube.
    //    private void drawScanLine(FieldProjectorEntity tile, PoseStack mx, MultiBufferSource buffers, AABB fieldBounds, double gameTime) {
//        VertexConsumer builder = buffers.getBuffer(RenderType.lines());
//
//        Vec3 tilePos = new Vec3(
//                tile.getBlockPos().getX() + 0.5d,
//                tile.getBlockPos().getY() + 0.5d,
//                tile.getBlockPos().getZ() + 0.5d
//        );
//
//        mx.pushPose();
//        mx.translate(.5, .5, .5);
//
//        int colorScanLine = getProjectionColor(EnumProjectorColorType.SCAN_LINE);
//
//        Direction face = tile.getProjectorSide();
//        Vec3 left = CubeRenderHelper.getScanLineLeft(face, fieldBounds, gameTime).subtract(tilePos);
//        Vec3 right = CubeRenderHelper.getScanLineRight(face, fieldBounds, gameTime).subtract(tilePos);
//
//        CubeRenderHelper.addColoredVertex(builder, mx, colorScanLine, left);
//        CubeRenderHelper.addColoredVertex(builder, mx, colorScanLine, right);
//
//        mx.popPose();
//        return bakedModelCached;
//    }

    /**
     * Handles drawing the projection arcs that connect the projector blocks to the main projection
     * in the center of the crafting area.
     */
    private record ScanArcGeometry(
            FieldProjectorRenderState renderState) implements SubmitNodeCollector.CustomGeometryRenderer {

        @Override
        public void render(PoseStack.@NonNull Pose pose, @NonNull VertexConsumer buffer) {
            try {
                pose.translate(.5f, .5f, .5f);

                int colorProjectionArc = ARGB.color(0.3f, ClientConfig.projectorColor);

                Vector3f centerOfProjector = Vec3.atCenterOf(renderState.blockPos).toVector3f();
                var scanLeft = ProjectorRenderHelper.getScanLineRight(
                        renderState.facing.getOpposite(),
                        renderState.fieldLocation.bounds(),
                        renderState.gameTime
                ).sub(centerOfProjector);

                var scanRight = ProjectorRenderHelper.getScanLineLeft(
                        renderState.facing.getOpposite(),
                        renderState.fieldLocation.bounds(),
                        renderState.gameTime
                ).sub(centerOfProjector);

                buffer.addVertex(pose, 0, 0.2f, 0)
                        .setColor(colorProjectionArc);

                buffer.addVertex(pose, scanLeft)
                        .setColor(colorProjectionArc);

                buffer.addVertex(pose, scanRight)
                        .setColor(colorProjectionArc);

                buffer.addVertex(pose, 0, 0.2f, 0)
                        .setColor(colorProjectionArc);
            } catch (Exception ex) {
                CompactCrafting.LOGGER.error(ex);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(FieldProjectorEntity blockEntity) {
        var state = blockEntity.getBlockState();
        var placement = ProjectorBlock.placement(GlobalPos.of(blockEntity.getLevel().dimension(), blockEntity.getBlockPos()), state);
        var fieldSize = ProjectorBlock.fieldSize(state);

        return MiniaturizationFieldLocation.compute(placement.position().dimension(), fieldSize, placement).bounds().inflate(12);
    }
}
