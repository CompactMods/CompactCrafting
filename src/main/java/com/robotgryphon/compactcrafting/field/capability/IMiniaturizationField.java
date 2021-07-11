package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface IMiniaturizationField {

    AxisAlignedBB getBounds();

    FieldProjectionSize getFieldSize();

    BlockPos getCenter();

    void setCenter(BlockPos center);

    void setSize(FieldProjectionSize size);

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    Optional<MiniaturizationRecipe> getCurrentRecipe();

    void clearRecipe();

    void completeCraft();

    EnumCraftingState getCraftingState();

    void doRecipeScan();

    void setCraftingState(EnumCraftingState state);

    default void tick() {}

    boolean isLoaded();

    default void checkLoaded() {}

    void markFieldChanged();

    void setLevel(World level);

    Set<BlockPos> getProxies();
    void registerProxyAt(BlockPos position);
    void unregisterProxyAt(BlockPos position);
}
