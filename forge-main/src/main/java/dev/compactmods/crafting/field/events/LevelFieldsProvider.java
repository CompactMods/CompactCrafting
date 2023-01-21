package dev.compactmods.crafting.field.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.core.CCCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

class LevelFieldsProvider implements ICapabilitySerializable<ListTag> {

    private final LazyOptional<IActiveWorldFields> inst;

    LevelFieldsProvider(IActiveWorldFields inst) {
        this.inst = LazyOptional.of(() -> inst);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

        if (cap == CCCapabilities.FIELDS)
            return inst.cast();

        return LazyOptional.empty();
    }

    @Override
    public ListTag serializeNBT() {
        return inst.map(INBTSerializable::serializeNBT).orElse(new ListTag());
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        inst.ifPresent(i -> i.deserializeNBT(nbt));
    }

    public void invalidate() {
        inst.invalidate();
    }
}
