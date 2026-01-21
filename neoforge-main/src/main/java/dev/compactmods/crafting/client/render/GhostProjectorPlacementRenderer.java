package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.core.CCAttachments;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record GhostProjectorPlacementRenderer(MiniaturizationFieldLocation field) {

    public void render(PoseStack matrixStack) {
        final var baseState = CCBlocks.FIELD_PROJECTOR_BLOCK.get().defaultBlockState();
        render(matrixStack, baseState);
    }

    public void render(PoseStack matrixStack, BlockState baseState) {
        final var mc = Minecraft.getInstance();
        final var player = mc.player;
        final var buffers = mc.renderBuffers().bufferSource();
        final var mainCamera = mc.gameRenderer.getMainCamera();
        final var level = mc.level;

        Objects.requireNonNull(level);
        Objects.requireNonNull(player);

        final int timeLeft = player.getData(CCAttachments.PLACEMENT_TIMER);
        if (timeLeft == 0)
            return;

        matrixStack.pushPose();
        Vec3 projectedView = mainCamera.position();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        renderSingleSet(matrixStack, baseState, buffers, level, Mth.clamp(timeLeft / 160f, 0.05f, 1f));

        matrixStack.popPose();

//        buffers.endBatch(CCRenderTypes.PHANTOM);
    }

    private void renderSingleSet(PoseStack poseStack, BlockState baseState, MultiBufferSource.BufferSource buffers, ClientLevel level, float alpha) {


        for (final var pos : field.projectors().locations()) {
            if (level.isEmptyBlock(pos.position())) {
                GhostRenderer.renderTransparentBlock(
                        baseState.setValue(BlockStateProperties.HORIZONTAL_FACING, pos.facing()),
                        pos.position(), poseStack, buffers, alpha, 0.9f);


                for (int y = 1; y < 10; y++) {
                    BlockPos realPos = pos.position().below(y);

                    if (!level.isEmptyBlock(realPos))
                        break;

                    GhostRenderer.renderTransparentBlock(Blocks.BLACK_STAINED_GLASS.defaultBlockState(),
                            realPos,
                            poseStack, buffers, alpha, 0.9f);
                }
            }
        }

    }
}