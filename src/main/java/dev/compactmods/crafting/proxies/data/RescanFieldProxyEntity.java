package dev.compactmods.crafting.proxies.data;

import dev.compactmods.crafting.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RescanFieldProxyEntity extends BaseFieldProxyEntity {
    public RescanFieldProxyEntity(BlockPos pos, BlockState state) {
        super(Registration.RESCAN_PROXY_ENTITY.get(), pos, state);
    }
}
