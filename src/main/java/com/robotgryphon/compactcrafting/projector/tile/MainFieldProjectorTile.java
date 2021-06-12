package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.network.FieldActivatedPacket;
import com.robotgryphon.compactcrafting.network.FieldDeactivatedPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class MainFieldProjectorTile extends FieldProjectorTile implements ITickableTileEntity {

    private MiniaturizationField field = null;

    private LazyOptional<MiniaturizationField> fieldCap = LazyOptional.empty();

    public MainFieldProjectorTile() {
        super(Registration.MAIN_FIELD_PROJECTOR_TILE.get());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // Invalidate field
        invalidateField();
    }

    @Override
    public void tick() {
        if (this.field != null)
            field.tickCrafting(this.level);
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    public void doFieldCheck() {

        if (this.field != null)
            return;

        Optional<MiniaturizationField> field = MiniaturizationField.tryCreateFromProjector(level, this.worldPosition);
        if (field.isPresent()) {
            this.field = field.get();
            this.fieldCap.invalidate();
            this.fieldCap = LazyOptional.of(() -> this.field);

            if (level != null && !level.isClientSide) {
                this.field.doRecipeScan(level);
                this.setChanged();

                PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                        .with(() -> level.getChunkAt(this.worldPosition));

                NetworkHandler.MAIN_CHANNEL
                        .send(trk, new FieldActivatedPacket(this.field.getCenterPosition(), this.field.getFieldSize()));
            }
        } else {
            this.invalidateField();
        }
    }

    public void invalidateField() {
        if (field == null) {
            fieldCap.invalidate();
            fieldCap = LazyOptional.empty();
            return;
        }

        BlockPos center = this.field.getCenterPosition();
        FieldProjectionSize size = this.field.getFieldSize();

        this.field = null;
        this.fieldCap.invalidate();
        this.fieldCap = LazyOptional.empty();

        if (level == null || level.isClientSide)
            return;

        PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                .with(() -> level.getChunkAt(this.worldPosition));

        // TODO - Change to the projector positions instead of the field info
        NetworkHandler.MAIN_CHANNEL
                .send(trk, new FieldDeactivatedPacket(center, size));

        this.setChanged();
    }

    /**
     * Used by clients to update field information on activation/deactivation.
     *
     * @param field
     */
    public void setFieldInfo(MiniaturizationField field) {
        this.field = field;
        this.setChanged();
    }



    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        if (this.field != null) {
            return field.getBounds().inflate(10);
        }

        return super.getRenderBoundingBox();
    }

    @Override
    public Optional<BlockPos> getMainProjectorPosition() {
        return Optional.ofNullable(worldPosition);
    }

    @Override
    public Optional<MainFieldProjectorTile> getMainProjectorTile() {
        return Optional.of(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return fieldCap.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();

        if (this.field != null) {
            CompoundNBT fieldInfo = new CompoundNBT();
            fieldInfo.put("center", NBTUtil.writeBlockPos(this.field.getCenterPosition()));
            fieldInfo.putString("size", this.field.getFieldSize().name());
            tag.put("fieldInfo", fieldInfo);
        }

        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        if (tag.contains("fieldInfo")) {
            CompoundNBT fieldInfo = tag.getCompound("fieldInfo");
            BlockPos fCenter = NBTUtil.readBlockPos(fieldInfo.getCompound("center"));
            String sizeName = fieldInfo.getString("size");
            FieldProjectionSize size = FieldProjectionSize.valueOf(sizeName);

            this.field = MiniaturizationField.fromSizeAndCenter(size, fCenter);
            this.fieldCap = LazyOptional.of(() -> this.field);
        } else {
            this.invalidateField();
        }
    }
}
