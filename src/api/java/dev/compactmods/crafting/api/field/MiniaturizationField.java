package dev.compactmods.crafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public interface MiniaturizationField extends INBTSerializable<Tag> {

    default void dispose() {}

    AABB getBounds();

    FieldSize getFieldSize();

    BlockPos getCenter();

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    EnumCraftingState getCraftingState();

    Optional<IMiniaturizationRecipe> getCurrentRecipe();

    default void tick() {
    }

    int getProgress();
    void setProgress(int progress);

    void fieldContentsChanged();

    void registerListener(LazyOptional<IFieldListener> listener);

    default CompoundTag serverData() {
        return new CompoundTag();
    }

    default CompoundTag clientData() {
        CompoundTag data = new CompoundTag();
        data.putLong("center", getCenter().asLong());
        data.putString("size", getFieldSize().name());
        data.putString("state", getCraftingState().name());
        return data;
    }

    default void handleDestabilize() {}

    LazyOptional<MiniaturizationField> getRef();
    void setRef(LazyOptional<MiniaturizationField> ref);
}
