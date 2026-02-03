package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.client.render.geometry.GhostBlockGeometry;
import dev.compactmods.crafting.projector.FieldProjectorsCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Objects;

public record GhostProjectorPlacementRenderer(MiniaturizationFieldLocation field) {

    public void render(PoseStack poseStack, float alpha) {

        final var mc = Minecraft.getInstance();
        final var level = mc.level;

        final var baseState = FieldProjectorsCommon.INACTIVE_FIELD_PROJECTOR_BLOCK.get().defaultBlockState();
        final var nodeStore = mc.gameRenderer.getSubmitNodeStorage();

        Objects.requireNonNull(level);

        poseStack.pushPose();
        renderSingleSet(poseStack, baseState, nodeStore, level, alpha);
        poseStack.popPose();
    }

    private void renderSingleSet(PoseStack poseStack, BlockState baseState, SubmitNodeStorage nodeStorage, ClientLevel level, float alpha) {

        final var pillarState = Blocks.BLACK_STAINED_GLASS.defaultBlockState();

        for (final var pos : field.projectors().locations()) {

            if (level.isEmptyBlock(pos.position())) {
                final var state = baseState.setValue(BlockStateProperties.HORIZONTAL_FACING, pos.facing());

                nodeStorage.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(),
                        new GhostBlockGeometry(pos.position(), state, alpha, 0.9f));

                for (int y = 1; y < 10; y++) {
                    BlockPos realPos = pos.position().below(y);

                    if (!level.isEmptyBlock(realPos))
                        break;

                    nodeStorage.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(),
                            new GhostBlockGeometry(realPos, pillarState, alpha, 0.9f));
                }
            }
        }

    }
}