package dev.compactmods.compactcrafting.api.field;

import dev.compactmods.compactcrafting.api.crafting.EnumCraftingState;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
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

    Optional<IMiniaturizationRecipe> getCurrentRecipe();

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
