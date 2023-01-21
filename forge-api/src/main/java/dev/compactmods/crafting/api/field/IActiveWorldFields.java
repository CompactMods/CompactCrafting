package dev.compactmods.crafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public interface IActiveWorldFields extends INBTSerializable<ListTag> {

    void setLevel(Level level);

    Stream<IMiniaturizationField> getFields();

    void tickFields();

    /**
     * Adds a field instance. This is typically called during world load; use this safely.
     * @param field The field to register.
     */
    default void addFieldInstance(IMiniaturizationField field) {}
    IMiniaturizationField registerField(IMiniaturizationField field);

    void unregisterField(BlockPos center);
    void unregisterField(IMiniaturizationField field);

    Optional<IMiniaturizationField> get(BlockPos center);

    LazyOptional<IMiniaturizationField> getLazy(BlockPos center);

    boolean hasActiveField(BlockPos center);

    Stream<IMiniaturizationField> getFields(ChunkPos chunk);

    ResourceKey<Level> getLevel();
}
