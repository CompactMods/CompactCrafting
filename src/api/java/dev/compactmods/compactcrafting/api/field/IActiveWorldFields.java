package dev.compactmods.compactcrafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public interface IActiveWorldFields {

    void setLevel(World level);

    Stream<IMiniaturizationField> getFields();

    void tickFields();

    void registerField(IMiniaturizationField field);

    void unregisterField(BlockPos center);
    void unregisterField(IMiniaturizationField field);

    Optional<IMiniaturizationField> get(BlockPos center);

    LazyOptional<IMiniaturizationField> getLazy(BlockPos center);

    boolean hasActiveField(BlockPos center);

    Stream<IMiniaturizationField> getFields(ChunkPos chunk);
}
