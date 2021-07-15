package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.data.NbtListCollector;
import com.robotgryphon.compactcrafting.field.ActiveWorldFields;
import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IActiveWorldFields;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityActiveWorldFields {

    @CapabilityInject(IActiveWorldFields.class)
    public static Capability<IActiveWorldFields> ACTIVE_WORLD_FIELDS = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IActiveWorldFields.class,
                new Capability.IStorage<IActiveWorldFields>() {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<IActiveWorldFields> capability, IActiveWorldFields instance, Direction side) {
                        ListNBT list = instance.getFields()
                                .map(field -> {
                                    // do stuff
                                    CompoundNBT nbt = new CompoundNBT();
                                    nbt.putString("size", field.getFieldSize().name());
                                    nbt.put("center", NBTUtil.writeBlockPos(field.getCenter()));
                                    return nbt;
                                })
                            .collect(NbtListCollector.toNbtList());

                        return list;
                    }

                    @Override
                    public void readNBT(Capability<IActiveWorldFields> capability, IActiveWorldFields instance, Direction side, INBT nbt) {
                        if(!(nbt instanceof ListNBT))
                            return;

                        ListNBT list = (ListNBT) nbt;

                        list.forEach(item -> {
                            if(!(item instanceof CompoundNBT))
                                return;

                            CompoundNBT f = (CompoundNBT) item;
                            FieldProjectionSize size = FieldProjectionSize.valueOf(f.getString("size"));
                            BlockPos center = NBTUtil.readBlockPos(f.getCompound("center"));

                            MiniaturizationField field = MiniaturizationField.fromSizeAndCenter(size, center);
                            instance.registerField(field);
                        });
                    }
                },
                ActiveWorldFields::new
        );
    }
}
