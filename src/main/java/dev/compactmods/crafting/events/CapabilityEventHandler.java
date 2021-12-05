package dev.compactmods.crafting.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.projector.IProjectorRenderInfo;
import dev.compactmods.crafting.client.render.ClientProjectorRenderInfo;
import dev.compactmods.crafting.core.CCCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEventHandler {

    @SubscribeEvent
    public static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            ClientProjectorRenderInfo backend = new ClientProjectorRenderInfo();
            LazyOptional<IProjectorRenderInfo> renderData = LazyOptional.of(() -> backend);

            event.addCapability(new ResourceLocation(CompactCrafting.MOD_ID, "projector_renderer"), new ICapabilityProvider() {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    if (cap == CCCapabilities.TEMP_PROJECTOR_RENDERING)
                        return renderData.cast();

                    return LazyOptional.empty();
                }
            });

            event.addListener(renderData::invalidate);
        }
    }
}
