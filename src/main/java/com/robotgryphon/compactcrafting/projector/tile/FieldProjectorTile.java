package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.IActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.network.FieldActivatedPacket;
import com.robotgryphon.compactcrafting.network.FieldDeactivatedPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.projector.ProjectorHelper;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class FieldProjectorTile extends TileEntity {

    protected BlockPos fieldCenter;
    protected LazyOptional<IMiniaturizationField> fieldCap = LazyOptional.empty();
    protected LazyOptional<IActiveWorldFields> levelFields = LazyOptional.empty();

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        return bs.getValue(FieldProjectorBlock.FACING);
    }

    public Direction getProjectorSide() {
        Direction facing = getFacing();
        return facing.getOpposite();
    }

    public boolean isMainProjector() {
        Direction side = getProjectorSide();

        // We're the main projector if we're to the NORTH
        return side == Direction.NORTH;
    }

    @Override
    public void setLevelAndPosition(World level, BlockPos position) {
        super.setLevelAndPosition(level, position);

        this.levelFields = level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);

        levelFields.lazyMap(fields -> fields.getLazy(fieldCenter))
                .ifPresent(field -> this.fieldCap = field);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // Invalidate field
        invalidateField();
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    public void tryActivateField() {
        if (fieldCap.isPresent())
            return;

        Optional<FieldProjectionSize> size = ProjectorHelper.getClosestOppositeSize(level, this.worldPosition, getFacing());
        if (!size.isPresent()) {
            invalidateField();
            return;
        }

        FieldProjectionSize foundSize = size.get();

        this.fieldCap.invalidate();

        BlockPos center = foundSize.getCenterFromProjector(this.worldPosition, getFacing());

        MiniaturizationField f = MiniaturizationField.fromSizeAndCenter(foundSize, center);

        levelFields.ifPresent(af -> {
            af.activateField(f);
            this.fieldCap = af.getLazy(f.getCenterPosition());
        });

        if (level != null && !level.isClientSide) {
            this.setChanged();

            PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                    .with(() -> level.getChunkAt(this.worldPosition));

            NetworkHandler.MAIN_CHANNEL
                    .send(trk, new FieldActivatedPacket(f.getCenterPosition(), f.getFieldSize()));
        }
    }

    public void invalidateField() {
        fieldCap.ifPresent(f -> {
            fieldCap.invalidate();
            levelFields.ifPresent(fields -> {
                fields.deactivateField(f);
            });

            if (level == null || level.isClientSide)
                return;

            PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                    .with(() -> level.getChunkAt(this.worldPosition));

            NetworkHandler.MAIN_CHANNEL
                    .send(trk, new FieldDeactivatedPacket(f.getCenterPosition(), f.getFieldSize()));
        });

        this.fieldCap = LazyOptional.empty();
        this.setChanged();
    }

    public void setField(LazyOptional<IMiniaturizationField> field) {
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

        fieldCap.ifPresent(field -> {
            tag.put("center", NBTUtil.writeBlockPos(field.getCenterPosition()));
        });

        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains("center")) {
            this.fieldCenter = NBTUtil.readBlockPos(tag.getCompound("center"));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        return super.getRenderBoundingBox().inflate(20);
    }
}
