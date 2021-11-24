package dev.compactmods.crafting.capability;

import java.util.Set;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IProjectorRenderInfo {

    Set<BlockPos> getMissingProjectors();

    int getRenderTimeLeft();

    void render(MatrixStack matrixStack);

    void tick();

    void resetRenderTime();

    void setProjector(World world, BlockPos pos);
}
