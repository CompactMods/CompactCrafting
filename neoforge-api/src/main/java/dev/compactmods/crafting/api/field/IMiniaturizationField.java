package dev.compactmods.crafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationField {

    default void dispose() {}

    AABB getBounds();

    MiniaturizationFieldSize getFieldSize();

    BlockPos getCenter();

    void setCenter(BlockPos center);

    void setSize(MiniaturizationFieldSize size);

    int getProgress();

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    void clearRecipe();

    EnumCraftingState getCraftingState();

    void setCraftingState(EnumCraftingState state);

    default void tick() {
    }

    boolean isLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    void setLevel(Level level);

    void setProgress(int progress);

    default void handleDestabilize() {}

    void enable();
    void disable();
    void checkRedstone();

    boolean enabled();
}
