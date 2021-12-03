package dev.compactmods.crafting.field.capability;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityMiniaturizationField {

    public static Capability<IMiniaturizationField> MINIATURIZATION_FIELD = CapabilityManager.get(new CapabilityToken<IMiniaturizationField>() {
    });

    public static void setup() {
        // TODO - Storage
//        CapabilityManager.INSTANCE.register(
//                IMiniaturizationField.class,
//                new Capability.IStorage<IMiniaturizationField>() {
//                    @Nullable
//                    @Override
//                    public Tag writeNBT(Capability<IMiniaturizationField> capability, IMiniaturizationField instance, Direction side) {
//                        CompoundTag fieldInfo = new CompoundTag();
//                        fieldInfo.put("center", NbtUtils.writeBlockPos(instance.getCenter()));
//                        fieldInfo.putString("size", instance.getFieldSize().name());
//
//                        fieldInfo.putString("craftingState", instance.getCraftingState().name());
//
//                        instance.getCurrentRecipe().ifPresent(rec -> {
//                            fieldInfo.putString("recipe", rec.getRecipeIdentifier().toString());
//                        });
//
////                        Set<BlockPos> proxies = instance.getProxies();
////                        if(!proxies.isEmpty()) {
////                            ListNBT proxyList = proxies.stream()
////                                    .map(NBTUtil::writeBlockPos)
////                                    .collect(NbtListCollector.toNbtList());
////
////                            fieldInfo.put("proxies", proxyList);
////                        }
//
//                        return fieldInfo;
//                    }
//
//                    @Override
//                    public void readNBT(Capability<IMiniaturizationField> capability, IMiniaturizationField instance, Direction side, Tag nbt) {
//                        if(nbt instanceof CompoundTag) {
//                            CompoundTag fieldInfo = (CompoundTag) nbt;
//
//                            BlockPos center = NbtUtils.readBlockPos(fieldInfo.getCompound("center"));
//                            MiniaturizationFieldSize size = MiniaturizationFieldSize.valueOf(fieldInfo.getString("size"));
//
//                            if (fieldInfo.contains("craftingState")) {
//                                EnumCraftingState state = EnumCraftingState.valueOf(fieldInfo.getString("craftingState"));
//                                instance.setCraftingState(state);
//                            }
//
////                            if(fieldInfo.contains("proxies")) {
////                                ListNBT proxies = fieldInfo.getList("proxies", Constants.NBT.TAG_COMPOUND);
////                                proxies.forEach(t -> {
////                                    BlockPos proxLocation = NBTUtil.readBlockPos((CompoundNBT) t);
////                                    instance.registerListener(proxLocation);
////                                });
////                            }
//
//                            instance.setCenter(center);
//                            instance.setSize(size);
//                        }
//                    }
//                },
//                MiniaturizationField::new);
    }
}
