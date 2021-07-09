package com.robotgryphon.compactcrafting.compat.theoneprobe.providers;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.proxies.block.FieldProxyBlock;
import com.robotgryphon.compactcrafting.proxies.data.FieldProxyTile;
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

        FieldProxyTile tile = (FieldProxyTile) level.getBlockEntity(hitData.getPos());
        if(tile == null)
            return;

        tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                .ifPresent(field -> {
                    BlockPos fieldCenter = field.getCenter();

                    info.text(new TranslationTextComponent("tooltip.compactcrafting.proxy_bound", fieldCenter.toString()));
                });
    }
}
