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

    Stream<MiniaturizationField> getFields();

    void tickFields();

    /**
     * Adds a field instance. This is typically called during world load; use this safely.
     * @param field The field to register.
     */
    default void addFieldInstance(MiniaturizationField field) {}
    MiniaturizationField registerField(MiniaturizationField field);

    void unregisterField(BlockPos center);
    void unregisterField(MiniaturizationField field);

    Optional<MiniaturizationField> get(BlockPos center);

    LazyOptional<MiniaturizationField> getLazy(BlockPos center);

    boolean hasActiveField(BlockPos center);

    Stream<MiniaturizationField> getFields(ChunkPos chunk);

    ResourceKey<Level> getLevel();
}
