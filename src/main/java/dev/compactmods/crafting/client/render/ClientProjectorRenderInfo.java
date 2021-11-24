package dev.compactmods.crafting.client.render;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.capability.IProjectorRenderInfo;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.ProjectorHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ClientProjectorRenderInfo implements IProjectorRenderInfo {

    private final BlockState baseState;
    private final HashMap<BlockPos, Direction> remainingProjectors;
    private int renderTime;


    public ClientProjectorRenderInfo() {
        this.remainingProjectors = new HashMap<>(4);
        this.baseState = Registration.FIELD_PROJECTOR_BLOCK.get().defaultBlockState();
    }

    @Override
    public Set<BlockPos> getMissingProjectors() {
        return remainingProjectors.keySet();
    }

    @Override
    public int getRenderTimeLeft() {
        return renderTime;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (this.renderTime == 0) return;
        if (this.remainingProjectors.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        final ActiveRenderInfo mainCamera = mc.gameRenderer.getMainCamera();
        final ClientWorld level = mc.level;

        matrixStack.pushPose();
        Vector3d projectedView = mainCamera.getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        remainingProjectors.forEach((pos, dir) -> {
            matrixStack.pushPose();
            matrixStack.translate(
                    (double) pos.getX() + 0.05,
                    (double) pos.getY() + 0.05,
                    (double) pos.getZ() + 0.05
            );

            matrixStack.scale(0.9f, 0.9f, 0.9f);

            GhostRenderer.renderTransparentBlock(baseState.setValue(FieldProjectorBlock.FACING, dir), pos, matrixStack, buffers, renderTime);
            matrixStack.popPose();

            for (int y = -1; y > -10; y--) {
                BlockPos realPos = new BlockPos(pos.getX(), pos.getY() + y, pos.getZ());
                if(!level.isStateAtPosition(realPos, AbstractBlock.AbstractBlockState::isAir))
                    break;

                matrixStack.pushPose();
                matrixStack.translate(
                        (double) pos.getX() + 0.15,
                        (double) pos.getY() + 0.15 + y,
                        (double) pos.getZ() + 0.15
                );

                matrixStack.scale(0.6f, 0.6f, 0.6f);
                GhostRenderer.renderTransparentBlock(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), null, matrixStack, buffers, renderTime);
                matrixStack.popPose();
            }
        });

        matrixStack.popPose();
        buffers.endBatch();
    }

    @Override
    public void tick() {
        if (renderTime > 0) this.renderTime--;
        if (renderTime < 0) this.renderTime = 0;
    }

    @Override
    public void resetRenderTime() {
        this.renderTime = ClientConfig.placementTime;
    }

    @Override
    public void setProjector(World level, BlockPos initial) {
        remainingProjectors.clear();

        Optional<MiniaturizationFieldSize> fieldSize = ProjectorHelper.getClosestOppositeSize(level, initial);
        final Direction initialFacing = FieldProjectorBlock.getDirection(level, initial).orElse(Direction.UP);
        if(fieldSize.isPresent()) {
            final BlockPos center = fieldSize.get().getCenterFromProjector(initial, initialFacing);
            Direction.Plane.HORIZONTAL.stream()
                    .forEach(dir -> {
                        if(dir == initialFacing) return;
                        
                        final BlockPos location = fieldSize.get().getProjectorLocationForDirection(center, dir);
                        remainingProjectors.put(location, dir.getOpposite());
                    });
        } else {
            ProjectorHelper.getValidOppositePositions(initial, initialFacing)
                    .forEach(pos -> this.remainingProjectors.put(pos, initialFacing.getOpposite()));
        }
    }
}
