package dev.compactmods.crafting.proxies.data;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class BaseFieldProxyEntity extends BlockEntity {

    @Nullable
    public BlockPos fieldCenter;
    
    @Nullable
    protected IMiniaturizationField<MiniaturizationRecipe> field = null;
    
    private UUID proxyId = UUID.randomUUID();

    public BaseFieldProxyEntity(BlockEntityType<? extends BaseFieldProxyEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        
        if(fieldCenter != null && level != null) {
            reconnectToField();
        }
    }
    
    private void reconnectToField() {
        if (fieldCenter == null || level == null) return;
        
        var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
        var fieldOpt = fields.get(fieldCenter);

        if (fieldOpt.isPresent()) {
            fieldChanged(fieldOpt.get());
        } else {
            fields.registerDisconnectedProxy(this);
        }
    }
    
    public boolean tryReconnectToField() {
        if (field == null && fieldCenter != null) {
            var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
            var fieldOpt = fields.get(fieldCenter);
            
            if (fieldOpt.isPresent()) {
                fieldChanged(fieldOpt.get());
                return true;
            }
        }
        return false;
    }

    public void updateField(BlockPos fieldCenter) {
        if (level == null)
            return;

        if(fieldCenter == null) {
            this.field = null;
            this.fieldCenter = null;
            return;
        }

        var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
        fields.get(fieldCenter).ifPresent(f -> {
            this.fieldCenter = fieldCenter;
            fieldChanged(f);
        });
        
        setChanged();
    }
    
    protected void fieldChanged(IMiniaturizationField<MiniaturizationRecipe> f) {
        this.field = f;
    }
    
    public Optional<IMiniaturizationField<MiniaturizationRecipe>> getField() {
        return Optional.ofNullable(field);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if(this.fieldCenter != null) {
            tag.put("center", NbtUtils.writeBlockPos(this.fieldCenter));
        }
        tag.putUUID("proxyId", proxyId);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if(tag.contains("center")) {
            this.fieldCenter = NbtUtils.readBlockPos(tag, "center").orElse(null);
        }
        if(tag.hasUUID("proxyId")) {
            this.proxyId = tag.getUUID("proxyId");
        }
    }
    
    public UUID getProxyId() {
        return proxyId;
    }
}