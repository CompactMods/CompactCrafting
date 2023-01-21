package dev.compactmods.crafting.proxies.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class BaseFieldProxyEntity extends BlockEntity {

    @Nullable
    protected BlockPos fieldCenter;

    protected LazyOptional<IMiniaturizationField> field = LazyOptional.empty();

    public BaseFieldProxyEntity(BlockEntityType<? extends BaseFieldProxyEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if(fieldCenter != null && level != null) {
            level.getCapability(CCCapabilities.FIELDS)
                    .resolve()
                    .ifPresent(fields -> fieldChanged(fields.getLazy(fieldCenter)));
        }
    }

    public void updateField(BlockPos fieldCenter) {
        if(level == null)
            return;

        if(fieldCenter == null) {
            this.field = LazyOptional.empty();
            this.fieldCenter = null;
            return;
        }

        level.getCapability(CCCapabilities.FIELDS)
                .map(fields -> fields.getLazy(fieldCenter))
                .ifPresent(f -> {
                    this.fieldCenter = fieldCenter;

                    fieldChanged(f);
                });
    }

    protected void fieldChanged(LazyOptional<IMiniaturizationField> f) {
        this.field = f;

        // field invalidated somewhere
        f.addListener(lof -> {
            this.field = LazyOptional.empty();
            this.fieldCenter = null;
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CCCapabilities.MINIATURIZATION_FIELD)
            return field.cast();

        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        field.ifPresent(field -> {
            tag.put("center", NbtUtils.writeBlockPos(field.getCenter()));
        });
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if(tag.contains("center")) {
            this.fieldCenter = NbtUtils.readBlockPos(tag.getCompound("center"));
        }
    }
}
