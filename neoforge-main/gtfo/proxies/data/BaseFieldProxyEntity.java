package dev.compactmods.crafting.proxies.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFieldProxyEntity extends BlockEntity {

    @Nullable
    protected BlockPos fieldCenter;

    public BaseFieldProxyEntity(BlockEntityType<? extends BaseFieldProxyEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void updateField(BlockPos fieldCenter) {
        if (level == null)
            return;

        this.fieldCenter = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        if(this.fieldCenter != null)
            tag.put("center", NbtUtils.writeBlockPos(this.fieldCenter));
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if(tag.contains("center")) {
            this.fieldCenter = NbtUtils.readBlockPos(tag.getCompound("center"));
        }
    }
}
