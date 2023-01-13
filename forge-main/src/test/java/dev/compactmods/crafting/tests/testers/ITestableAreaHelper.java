package dev.compactmods.crafting.tests.testers;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface ITestableAreaHelper {

    @NotNull
    Level getLevel();

    @NotNull
    AABB getTestBounds();

    static AABB getFieldBoundsInternal(MiniaturizationFieldSize fieldSize, BlockPos origin) {
        var bounds = fieldSize.getBoundsAtOrigin(origin.getY());
        return bounds.move(origin.getX(), 0, origin.getZ());
    }
}
