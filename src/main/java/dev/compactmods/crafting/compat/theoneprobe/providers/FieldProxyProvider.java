package dev.compactmods.crafting.compat.theoneprobe.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class FieldProxyProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return CompactCrafting.MOD_ID + "_proxy";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World level, BlockState state, IProbeHitData hitData) {
        if (!(state.getBlock() instanceof FieldProxyBlock))
            return;

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(hitData.getPos());
        if(tile == null)
            return;

        tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                .ifPresent(field -> {
                    BlockPos fieldCenter = field.getCenter();

                    info.text(new TranslationTextComponent("tooltip.compactcrafting.proxy_bound", fieldCenter.toString()));
                });
    }
}
