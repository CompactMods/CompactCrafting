package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.projector.IProjectorRenderInfo;
import dev.compactmods.crafting.client.render.ClientProjectorRenderInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CraftingCapabilities {

    public static final EntityCapability<IProjectorRenderInfo, Void> GHOST_PROJECTOR_RENDER = EntityCapability.createVoid(
            new ResourceLocation(CompactCrafting.MOD_ID, "ghost_projector_render"),
            IProjectorRenderInfo.class
    );

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent registerCaps) {
        registerCaps.registerEntity(GHOST_PROJECTOR_RENDER,
                EntityType.PLAYER,
                (player, v) -> new ClientProjectorRenderInfo());
    }
}
