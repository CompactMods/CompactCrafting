package dev.compactmods.crafting.compat.theoneprobe.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FieldProxyProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CompactCrafting.MOD_ID, "proxy");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
        if (!(state.getBlock() instanceof FieldProxyBlock))
            return;

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(hitData.getPos());
        if(tile == null)
            return;

        tile.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                .ifPresent(field -> {
                    BlockPos fieldCenter = field.getCenter();

                    info.text(new TranslatableComponent("tooltip.compactcrafting.proxy_bound", fieldCenter.toString()));
                });
    }
}
