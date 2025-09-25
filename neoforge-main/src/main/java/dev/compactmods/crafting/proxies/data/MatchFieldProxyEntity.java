package dev.compactmods.crafting.proxies.data;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.proxies.listener.MatchModeProxyFieldListener;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MatchFieldProxyEntity extends BaseFieldProxyEntity {
    
    protected MatchModeProxyFieldListener listener;

    public MatchFieldProxyEntity(BlockPos pos, BlockState state) {
        super(CCBlocks.MATCH_PROXY_ENTITY.get(), pos, state);
    }
    
    @Override
    protected void fieldChanged(IMiniaturizationField<MiniaturizationRecipe> f) {
        super.fieldChanged(f);

        this.listener = new MatchModeProxyFieldListener(level, worldPosition);

        if (f instanceof MiniaturizationField mf) {
            mf.registerListener(this.listener);
        }
    }
    
    @Override
    public void setRemoved() {
        if (field instanceof MiniaturizationField mf && listener != null) {
            mf.unregisterListener(this.listener);
        }
        super.setRemoved();
    }
}