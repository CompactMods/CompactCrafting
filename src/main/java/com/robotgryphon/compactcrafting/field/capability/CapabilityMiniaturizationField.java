package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import dev.compactmods.compactcrafting.api.crafting.EnumCraftingState;
import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityMiniaturizationField {
    @CapabilityInject(IMiniaturizationField.class)
    public static Capability<IMiniaturizationField> MINIATURIZATION_FIELD = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IMiniaturizationField.class,
                new Capability.IStorage<IMiniaturizationField>() {
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

//                        Set<BlockPos> proxies = instance.getProxies();
//                        if(!proxies.isEmpty()) {
//                            ListNBT proxyList = proxies.stream()
//                                    .map(NBTUtil::writeBlockPos)
//                                    .collect(NbtListCollector.toNbtList());
//
//                            fieldInfo.put("proxies", proxyList);
//                        }

                        return fieldInfo;
                    }

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

//                            if(fieldInfo.contains("proxies")) {
//                                ListNBT proxies = fieldInfo.getList("proxies", Constants.NBT.TAG_COMPOUND);
//                                proxies.forEach(t -> {
//                                    BlockPos proxLocation = NBTUtil.readBlockPos((CompoundNBT) t);
//                                    instance.registerListener(proxLocation);
//                                });
//                            }

                            instance.setCenter(center);
                            instance.setSize(size);
                        }
                    }
                },
                MiniaturizationField::new);
    }
}
