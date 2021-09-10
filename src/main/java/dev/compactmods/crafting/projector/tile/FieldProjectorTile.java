package dev.compactmods.crafting.projector.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.projector.block.FieldProjectorBlock;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
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

public class FieldProjectorTile extends TileEntity {

    private MiniaturizationFieldSize fieldSize;
    private AxisAlignedBB fieldBounds;

    protected LazyOptional<IMiniaturizationField> fieldCap = LazyOptional.empty();
    protected LazyOptional<IActiveWorldFields> levelFields = LazyOptional.empty();

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public FieldProjectorTile(MiniaturizationFieldSize size) {
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

        if (this.level != null) {
            this.levelFields = level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);
        }

        levelFields.ifPresent(fields -> {
            if(!level.isClientSide && !fields.hasActiveField(center)) {
                fields.registerField(MiniaturizationField.fromSizeAndCenter(fieldSize, center));
            }

            this.fieldCap = fields.getLazy(center);
        });
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

    public LazyOptional<IMiniaturizationField> getField() {
        return this.fieldCap;
    }
}
