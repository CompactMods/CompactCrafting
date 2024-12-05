package dev.compactmods.crafting.api.projector;

import java.util.Set;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IProjectorRenderInfo {

    Set<BlockPos> getMissingProjectors();

    int getRenderTimeLeft();

    void render(PoseStack matrixStack);

    void tick();

    void resetRenderTime();

    void setProjector(Level world, BlockPos pos);
}
