package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;
import java.util.Set;

public record FieldProjectorSet(WeakReference<Level> level, Set<BlockPos> locations, MiniaturizationFieldSize fieldSize) {
    public void enableAll() {
        final var level1 = level.get();
        locations.forEach(pos -> {
            BlockState currentState = level1.getBlockState(pos);
            if (currentState.hasProperty(FieldProjectorProperties.SIZE) &&
                    currentState.getValue(FieldProjectorProperties.SIZE) != fieldSize) {

                BlockState newState = currentState.setValue(FieldProjectorProperties.SIZE, fieldSize);
                level1.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        });
    }

    public void disableAll() {
        final var level1 = level.get();
        locations.forEach(pos -> {
            BlockState currentState = level1.getBlockState(pos);
            if (currentState.hasProperty(FieldProjectorProperties.SIZE)) {
                BlockState newState = currentState.setValue(FieldProjectorProperties.SIZE, MiniaturizationFieldSize.INACTIVE);
                level1.setBlock(pos, newState, Block.UPDATE_ALL);
                level1.removeBlockEntity(pos);
            }
        });
    }
}
