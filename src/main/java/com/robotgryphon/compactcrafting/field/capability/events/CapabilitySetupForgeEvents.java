package com.robotgryphon.compactcrafting.field.capability.events;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.ActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IActiveWorldFields;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilitySetupForgeEvents {

    @SubscribeEvent
    public static void onCapWorldAttach(final AttachCapabilitiesEvent<World> event) {
        World level = event.getObject();

        ActiveWorldFields inst = new ActiveWorldFields(level);

        LazyOptional<ActiveWorldFields> opt = LazyOptional.of(() -> inst);
        final Capability<IActiveWorldFields> capInstance = CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS;

        ICapabilityProvider provider = new ICapabilitySerializable<ListNBT>() {
            @Override
            public ListNBT serializeNBT() {
                return (ListNBT) capInstance.getStorage().writeNBT(capInstance, inst, null);
            }

            @Override
            public void deserializeNBT(ListNBT nbt) {
                capInstance.getStorage().readNBT(capInstance, inst, null, nbt);
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if(capInstance == cap)
                    return opt.cast();

                return LazyOptional.empty();
            }
        };

        event.addCapability(new ResourceLocation(CompactCrafting.MOD_ID, "world_fields"), provider);
        event.addListener(opt::invalidate);
    }
}
