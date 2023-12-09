package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.stream.Stream;

public interface IActiveWorldFields extends INBTSerializable<ListTag> {

    void setLevel(Level level);

    Stream<dev.compactmods.crafting.api.field.IMiniaturizationField> getFields();

    void tickFields();

    /**
     * Adds a field instance. This is typically called during world load; use this safely.
     * @param field The field to register.
     */
    default void addFieldInstance(dev.compactmods.crafting.api.field.IMiniaturizationField field) {}
    dev.compactmods.crafting.api.field.IMiniaturizationField registerField(dev.compactmods.crafting.api.field.IMiniaturizationField field);

    void unregisterField(BlockPos center);
    void unregisterField(dev.compactmods.crafting.api.field.IMiniaturizationField field);

    Optional<dev.compactmods.crafting.api.field.IMiniaturizationField> get(BlockPos center);

    boolean hasActiveField(BlockPos center);

    Stream<IMiniaturizationField> getFields(ChunkPos chunk);

    ResourceKey<Level> getLevel();
}
