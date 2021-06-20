package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.Stream;

public interface IMiniaturizationField {

    AxisAlignedBB getBounds();

    FieldProjectionSize getFieldSize();

    BlockPos getCenterPosition();

    void setCenter(BlockPos center);

    void setSize(FieldProjectionSize size);

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    Optional<MiniaturizationRecipe> getCurrentRecipe();

    void clearRecipe();

    void completeCraft();

    EnumCraftingState getCraftingState();

    void doRecipeScan(World level);

    void setCraftingState(EnumCraftingState state);

    default void tick(World level) {}
}
