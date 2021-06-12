package com.robotgryphon.compactcrafting.field.capability;

import net.minecraft.world.World;

import java.util.stream.Stream;

public interface IActiveWorldFields {

    Stream<IMiniaturizationField> getFields();

    void tickFields(World level);

    void activateField(IMiniaturizationField field);

    void deactivateField(IMiniaturizationField field);
}
