package dev.compactmods.compactcrafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.compactcrafting.api.crafting.EnumCraftingState;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public interface IMiniaturizationField {

    AxisAlignedBB getBounds();

    FieldProjectionSize getFieldSize();

    BlockPos getCenter();

    void setCenter(BlockPos center);

    void setSize(FieldProjectionSize size);

    int getProgress();

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

    void registerListener(LazyOptional<IFieldListener> listener);

    CompoundNBT save();

    void load(CompoundNBT nbt);
}
