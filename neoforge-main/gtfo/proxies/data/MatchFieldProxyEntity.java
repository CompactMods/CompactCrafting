package dev.compactmods.crafting.proxies.data;

import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MatchFieldProxyEntity extends BaseFieldProxyEntity {

    public MatchFieldProxyEntity(BlockPos pos, BlockState state) {
        super(CCBlocks.MATCH_PROXY_ENTITY.get(), pos, state);
    }

}
