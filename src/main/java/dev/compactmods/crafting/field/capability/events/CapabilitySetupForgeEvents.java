package dev.compactmods.crafting.field.capability.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.field.ActiveWorldFields;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilitySetupForgeEvents {

    @SubscribeEvent
    public static void onCapWorldAttach(final AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();

        ActiveWorldFields inst = new ActiveWorldFields(level);

        LazyOptional<ActiveWorldFields> opt = LazyOptional.of(() -> inst);
        final Capability<IActiveWorldFields> capInstance = CCCapabilities.FIELDS;

        event.addCapability(new ResourceLocation(CompactCrafting.MOD_ID, "world_fields"), new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if(capInstance == cap)
                    return opt.cast();

                return LazyOptional.empty();
            }
        });

        event.addListener(opt::invalidate);
    }
}
