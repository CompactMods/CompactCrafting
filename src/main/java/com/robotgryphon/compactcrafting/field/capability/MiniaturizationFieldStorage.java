package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.data.NbtListCollector;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Set;

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
        CompoundNBT fieldInfo = new CompoundNBT();
        fieldInfo.put("center", NBTUtil.writeBlockPos(instance.getCenter()));
        fieldInfo.putString("size", instance.getFieldSize().name());

        fieldInfo.putString("craftingState", instance.getCraftingState().name());

        instance.getCurrentRecipe().ifPresent(rec -> {
            fieldInfo.putString("recipe", rec.getId().toString());
        });

        Set<BlockPos> proxies = instance.getProxies();
        if(!proxies.isEmpty()) {
            ListNBT proxyList = proxies.stream()
                    .map(NBTUtil::writeBlockPos)
                    .collect(NbtListCollector.toNbtList());

            fieldInfo.put("proxies", proxyList);
        }

        return fieldInfo;
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
        if(nbt instanceof CompoundNBT) {
            CompoundNBT fieldInfo = (CompoundNBT) nbt;

            BlockPos center = NBTUtil.readBlockPos(fieldInfo.getCompound("center"));
            FieldProjectionSize size = FieldProjectionSize.valueOf(fieldInfo.getString("size"));

            if (fieldInfo.contains("craftingState")) {
                EnumCraftingState state = EnumCraftingState.valueOf(fieldInfo.getString("craftingState"));
                instance.setCraftingState(state);
            }

            if(fieldInfo.contains("proxies")) {
                ListNBT proxies = fieldInfo.getList("proxies", Constants.NBT.TAG_COMPOUND);
                proxies.forEach(t -> {
                    BlockPos proxLocation = NBTUtil.readBlockPos((CompoundNBT) t);
                    instance.registerProxyAt(proxLocation);
                });
            }

            instance.setCenter(center);
            instance.setSize(size);
        }
    }
}
