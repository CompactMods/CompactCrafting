package dev.compactmods.crafting.compat.theoneprobe.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FieldProxyProvider implements IProbeInfoProvider {
    private static final ResourceLocation ID = CompactCrafting.modRL("field_proxy");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
        if (!(state.getBlock() instanceof FieldProxyBlock))
            return;

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(hitData.getPos());
        if(tile == null || tile.fieldCenter == null)
            return;

        var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
        fields.get(tile.fieldCenter).ifPresent(field -> {
            BlockPos fieldCenter = field.getCenter();
            info.text(Component.translatable("compactcrafting.top.proxy_bound", fieldCenter.toString()));

            if (field.currentRecipe() != null) {
                info.text(Component.translatable("compactcrafting.top.proxy_has_recipe"));
            } else {
                info.text(Component.translatable("compactcrafting.top.proxy_no_recipe"));
            }
        });
    }
}
