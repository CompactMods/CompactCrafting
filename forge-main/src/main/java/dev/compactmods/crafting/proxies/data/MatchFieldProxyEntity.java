package dev.compactmods.crafting.proxies.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.listener.MatchModeProxyFieldListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class MatchFieldProxyEntity extends BaseFieldProxyEntity {
    protected LazyOptional<IFieldListener> listener = LazyOptional.empty();

    public MatchFieldProxyEntity(BlockPos pos, BlockState state) {
        super(CCBlocks.MATCH_PROXY_ENTITY.get(), pos, state);
    }

    @Override
    protected void fieldChanged(LazyOptional<IMiniaturizationField> f) {
        super.fieldChanged(f);

        MatchModeProxyFieldListener listener = new MatchModeProxyFieldListener(level, worldPosition);

        this.listener = LazyOptional.of(() -> listener);

        // if field actually present, register this proxy
        f.ifPresent(f2 -> f2.registerListener(this.listener));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CCCapabilities.FIELD_LISTENER)
            return listener.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        listener.invalidate();
    }
}
