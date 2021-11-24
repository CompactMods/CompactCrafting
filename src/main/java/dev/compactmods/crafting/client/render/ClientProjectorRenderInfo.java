package dev.compactmods.crafting.client.render;

import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.capability.IProjectorRenderInfo;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.ProjectorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ClientProjectorRenderInfo implements IProjectorRenderInfo {

    private final BlockState baseState;
    private Set<BlockPos> remainingProjectors;
    private int renderTime;


    public ClientProjectorRenderInfo() {
        this.baseState = Registration.FIELD_PROJECTOR_BLOCK.get().defaultBlockState();
    }

    @Override
    public Set<BlockPos> getMissingProjectors() {
        return remainingProjectors;
    }

    @Override
    public int getRenderTimeLeft() {
        return renderTime;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if(this.renderTime == 0) return;
        if(this.remainingProjectors.isEmpty()) return;

        final IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        final ActiveRenderInfo mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();

        matrixStack.pushPose();
        Vector3d projectedView = mainCamera.getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        remainingProjectors.forEach(pos -> {
            matrixStack.pushPose();
            matrixStack.translate(
                    (double) pos.getX(),
                    (double) pos.getY(),
                    (double) pos.getZ()
            );

            GhostRenderer.renderTransparentBlock(baseState,  matrixStack, buffers);
            matrixStack.popPose();
        });

        matrixStack.popPose();
    }

    @Override
    public void tick() {
        if(renderTime > 0)
            this.renderTime--;
        if(renderTime < 0)
            this.renderTime = 0;
    }

    @Override
    public void resetRenderTime() {
        this.renderTime = 20 * 10;
    }

    @Override
    public void setProjector(World level, BlockPos initial) {
        this.remainingProjectors = ProjectorHelper.getMissingProjectors(level, initial,
                        FieldProjectorBlock.getDirection(level, initial).orElse(Direction.UP))
                .collect(Collectors.toSet());
    }
}
