package com.robotgryphon.compactcrafting.field.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.stream.Stream;

public interface IActiveWorldFields {

    Stream<IMiniaturizationField> getFields();

    void tickFields(World level);

    void registerField(IMiniaturizationField field);

    void unregisterField(IMiniaturizationField field);

    Optional<IMiniaturizationField> get(BlockPos center);

    LazyOptional<IMiniaturizationField> getLazy(BlockPos center);

    boolean hasActiveField(BlockPos center);
}
