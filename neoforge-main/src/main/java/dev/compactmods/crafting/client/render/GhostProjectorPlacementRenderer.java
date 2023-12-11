package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.ProjectorHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Optional;

public class GhostProjectorPlacementRenderer {

    public static final HashMap<BlockPos, Direction> remainingProjectors= new HashMap<>(4);

    public static int renderTime = 0;

    public static void calculateMissingProjectors(Level level, BlockPos initial) {
        remainingProjectors.clear();

        final Direction initialFacing = FieldProjectorBlock.getDirection(level, initial).orElse(Direction.UP);
        Optional<MiniaturizationFieldSize> fieldSize = ProjectorHelper.getClosestSize(level, initial, initialFacing);

        if (fieldSize.isPresent()) {
            final BlockPos center = fieldSize.get().getCenterFromProjector(initial, initialFacing);
            Direction.Plane.HORIZONTAL.stream()
                    .forEach(dir -> {
                        if (dir.getOpposite() == initialFacing) return;
                        final BlockPos location = fieldSize.get().getProjectorLocationForDirection(center, dir);
                        if (!(level.getBlockState(location).getBlock() instanceof FieldProjectorBlock))
                            remainingProjectors.put(location, dir.getOpposite());
                    });
        } else {
            ProjectorHelper.getValidOppositePositions(initial, initialFacing)
                    .forEach(pos -> remainingProjectors.put(pos, initialFacing.getOpposite()));
        }
    }

    public static void render(PoseStack matrixStack) {
        final var baseState = CCBlocks.FIELD_PROJECTOR_BLOCK.get().defaultBlockState();
        render(matrixStack, baseState);
    }

    public static void render(PoseStack matrixStack, BlockState baseState) {
        if (renderTime == 0) return;
        if (remainingProjectors.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        final ClientLevel level = mc.level;

        matrixStack.pushPose();
        Vec3 projectedView = mainCamera.getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for(BlockPos pos : remainingProjectors.keySet()) {
            matrixStack.pushPose();
            matrixStack.translate(
                    (double) pos.getX() + 0.05,
                    (double) pos.getY() + 0.05,
                    (double) pos.getZ() + 0.05
            );

            matrixStack.scale(.9f, .9f, .9f);

            GhostRenderer.renderTransparentBlock(baseState.setValue(FieldProjectorBlock.FACING, remainingProjectors.get(pos)), pos, matrixStack, buffers, renderTime);
            matrixStack.popPose();

            for (int y = -1; y > -10; y--) {
                BlockPos realPos = new BlockPos(pos.getX(), pos.getY() + y, pos.getZ());
                if (!level.isStateAtPosition(realPos, BlockBehaviour.BlockStateBase::isAir))
                    break;

                matrixStack.pushPose();
                matrixStack.translate(
                        (double) pos.getX() + 0.15,
                        (double) pos.getY() + 0.15 + y,
                        (double) pos.getZ() + 0.15
                );

                matrixStack.scale(0.6f, 0.6f, 0.6f);
                GhostRenderer.renderTransparentBlock(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), realPos, matrixStack, buffers, renderTime);
                matrixStack.popPose();
            }
        }

        matrixStack.popPose();

        RenderSystem.disableDepthTest();
        buffers.endBatch(CCRenderTypes.PHANTOM);
    }

    public static void tick() {
        if (renderTime > 0) renderTime--;
        if (renderTime < 0) renderTime = 0;
    }

    public static void resetRenderTime() {
        renderTime = ClientConfig.placementTime;
    }

    public static void setOriginProjector(Level level, BlockPos initial) {
        calculateMissingProjectors(level, initial);
    }
}
