package dev.compactmods.crafting.field.capability;

import javax.annotation.Nullable;
import dev.compactmods.crafting.data.NbtListCollector;
import dev.compactmods.crafting.field.ActiveWorldFields;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

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
                        // do stuff
                        ListNBT list = instance.getFields()
                            .map(IMiniaturizationField::serverData)
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
                            MiniaturizationFieldSize size = MiniaturizationFieldSize.valueOf(f.getString("size"));
                            BlockPos center = NBTUtil.readBlockPos(f.getCompound("center"));

                            MiniaturizationField field = MiniaturizationField.fromSizeAndCenter(size, center);
                            field.loadServerData(f);

                            instance.addFieldInstance(field);
                        });
                    }
                },
                ActiveWorldFields::new
        );
    }
}
