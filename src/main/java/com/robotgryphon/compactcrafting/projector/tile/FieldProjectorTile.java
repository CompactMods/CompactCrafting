package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.Registration;
import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IActiveWorldFields;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import com.robotgryphon.compactcrafting.network.FieldDeactivatedPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class FieldProjectorTile extends TileEntity {

    private FieldProjectionSize fieldSize;
    private AxisAlignedBB fieldBounds;

    protected LazyOptional<IMiniaturizationField> fieldCap = LazyOptional.empty();
    protected LazyOptional<IActiveWorldFields> levelFields = LazyOptional.empty();

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public FieldProjectorTile(FieldProjectionSize size) {
        super(Registration.FIELD_PROJECTOR_TILE.get());
        this.fieldSize = size;
    }

    public Optional<AxisAlignedBB> getFieldBounds() {
        return Optional.ofNullable(fieldBounds);
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        return bs.getValue(FieldProjectorBlock.FACING);
    }

    public Direction getProjectorSide() {
        Direction facing = getFacing();
        return facing.getOpposite();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.fieldSize == null)
            return;

        BlockPos center;
        if (this.fieldBounds == null) {
            center = fieldSize.getCenterFromProjector(worldPosition, getFacing());
            fieldBounds = fieldSize.getBoundsAtPosition(center);
        } else {
            center = new BlockPos(fieldBounds.getCenter());
        }

        if (this.level != null && !level.isClientSide) {
            this.levelFields = level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);
            levelFields.ifPresent(af -> {
                if (!af.hasActiveField(center)) {
                    af.registerField(MiniaturizationField.fromSizeAndCenter(fieldSize, center));
                }

                this.fieldCap = af.getLazy(center);
            });
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // Invalidate field
        invalidateField();
    }

    public void invalidateField() {
        if (fieldBounds == null)
            return;

        BlockPos fieldCenter = new BlockPos(fieldBounds.getCenter());

        fieldSize.getProjectorLocations(fieldCenter)
                .filter(p -> level.getBlockState(p).getBlock() instanceof FieldProjectorBlock)
                .forEach(p -> FieldProjectorBlock.deactivateProjector(level, p));

        fieldCap.ifPresent(f -> {
            fieldCap.invalidate();
            levelFields.ifPresent(fields -> {
                fields.unregisterField(f);
            });

            if (level == null || level.isClientSide)
                return;

            PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                    .with(() -> level.getChunkAt(this.worldPosition));

            NetworkHandler.MAIN_CHANNEL
                    .send(trk, new FieldDeactivatedPacket(fieldSize, fieldCenter));
        });

        this.fieldCap = LazyOptional.empty();
        this.setChanged();
    }

    public void setField(LazyOptional<IMiniaturizationField> field) {
        this.fieldCap.invalidate();
        this.fieldCap = field;
        field.addListener(f -> {
            this.fieldCap = LazyOptional.empty();
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return fieldCap.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        CompoundNBT tag = super.save(nbt);

        if (fieldBounds != null)
            tag.put("center", NBTUtil.writeBlockPos(new BlockPos(fieldBounds.getCenter())));

        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains("center")) {
            BlockPos center = NBTUtil.readBlockPos(tag.getCompound("center"));
            this.fieldSize = state.getValue(FieldProjectorBlock.SIZE);

            if (FieldProjectorBlock.isActive(state)) {
                this.fieldBounds = fieldSize.getBoundsAtPosition(center);
            }
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();

        if (fieldBounds != null)
            tag.put("center", NBTUtil.writeBlockPos(new BlockPos(fieldBounds.getCenter())));

        return tag;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        if (fieldBounds != null)
            return fieldBounds.inflate(10);

        return new AxisAlignedBB(worldPosition).inflate(20);
    }
}
