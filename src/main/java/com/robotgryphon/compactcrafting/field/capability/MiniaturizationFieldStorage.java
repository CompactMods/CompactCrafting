package com.robotgryphon.compactcrafting.field.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class MiniaturizationFieldStorage implements Capability.IStorage<IMiniaturizationField> {
    /**
     * Serialize the capability instance to a NBTTag.
     * This allows for a central implementation of saving the data.
     * <p>
     * It is important to note that it is up to the API defining
     * the capability what requirements the 'instance' value must have.
     * <p>
     * Due to the possibility of manipulating internal data, some
     * implementations MAY require that the 'instance' be an instance
     * of the 'default' implementation.
     * <p>
     * Review the API docs for more info.
     *
     * @param capability The Capability being stored.
     * @param instance   An instance of that capabilities interface.
     * @param side       The side of the object the instance is associated with.
     * @return a NBT holding the data. Null if no data needs to be stored.
     */
    @Nullable
    @Override
    public INBT writeNBT(Capability<IMiniaturizationField> capability, IMiniaturizationField instance, Direction side) {
        return null;
    }

    /**
     * Read the capability instance from a NBT tag.
     * <p>
     * This allows for a central implementation of saving the data.
     * <p>
     * It is important to note that it is up to the API defining
     * the capability what requirements the 'instance' value must have.
     * <p>
     * Due to the possibility of manipulating internal data, some
     * implementations MAY require that the 'instance' be an instance
     * of the 'default' implementation.
     * <p>
     * Review the API docs for more info.         *
     *
     * @param capability The Capability being stored.
     * @param instance   An instance of that capabilities interface.
     * @param side       The side of the object the instance is associated with.
     * @param nbt        A NBT holding the data. Must not be null, as doesn't make sense to call this function with nothing to read...
     */
    @Override
    public void readNBT(Capability<IMiniaturizationField> capability, IMiniaturizationField instance, Direction side, INBT nbt) {

    }
}
