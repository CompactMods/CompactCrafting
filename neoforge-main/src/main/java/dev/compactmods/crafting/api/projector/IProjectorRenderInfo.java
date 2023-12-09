package dev.compactmods.crafting.api.projector;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;

public interface IProjectorRenderInfo {

    Set<BlockPos> getMissingProjectors();

    int getRenderTimeLeft();

    void render(PoseStack matrixStack);

    void tick();

    void resetRenderTime();

    void setProjector(Level world, BlockPos pos);
}
