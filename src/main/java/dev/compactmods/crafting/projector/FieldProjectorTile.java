package dev.compactmods.crafting.projector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.network.FieldActivatedPacket;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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

    public FieldProjectorTile(IBlockReader level, BlockState state) {
        super(Registration.FIELD_PROJECTOR_TILE.get());

        this.fieldSize = state.getValue(FieldProjectorBlock.SIZE);
    }

    public Optional<AxisAlignedBB> getFieldBounds() {
        return Optional.ofNullable(fieldBounds);
    }

    public Direction getProjectorSide() {
        return getBlockState().getValue(FieldProjectorBlock.FACING).getOpposite();
    }

    @Override
    public void setPosition(BlockPos position) {
        super.setPosition(position);
        if (level.isClientSide) return;
        final MinecraftServer server = this.level.getServer();
        if (server == null) return;
        server.submitAsync(new TickDelayedTask(server.getTickCount() + 3, this::updateFieldInfo));
    }

    @Override
    public void setLevelAndPosition(World level, BlockPos position) {
        super.setLevelAndPosition(level, position);
        if (level.isClientSide) return;
        final MinecraftServer server = this.level.getServer();
        if (server == null) return;
        server.submitAsync(new TickDelayedTask(server.getTickCount() + 3, this::updateFieldInfo));
    }

    public void updateFieldInfo() {
        if (this.level == null)
            return;

        this.levelFields = level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);

        BlockPos center = BlockPos.ZERO;
        final BlockState state = getBlockState();
        if (this.fieldBounds == null) {
            center = fieldSize.getCenterFromProjector(worldPosition, state.getValue(FieldProjectorBlock.FACING));
            fieldBounds = fieldSize.getBoundsAtPosition(center);
        } else {
            center = new BlockPos(fieldBounds.getCenter());
        }

        final BlockPos finalCenter = center.immutable();
        levelFields.ifPresent(fields -> {
            if (!fields.hasActiveField(finalCenter)) {
                final IMiniaturizationField field = fields.registerField(MiniaturizationField.fromSizeAndCenter(fieldSize, finalCenter));

                if(!level.isClientSide) {
                    // Send activation packet to clients
                    NetworkHandler.MAIN_CHANNEL.send(
                            PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(field.getCenter())),
                            new FieldActivatedPacket(field));
                }
            }

            CompactCrafting.LOGGER.debug("Changing field: updateFI");
            this.fieldCap = fields.getLazy(finalCenter);
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
            CompactCrafting.LOGGER.debug("Unsetting field: invalidate");
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

        CompactCrafting.LOGGER.debug("Unsetting field: invalidate");
        this.fieldCap = LazyOptional.empty();
        this.setChanged();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return fieldCap.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        this.fieldSize = state.getValue(FieldProjectorBlock.SIZE);

        BlockPos center = fieldSize.getCenterFromProjector(worldPosition, state.getValue(FieldProjectorBlock.FACING));
        this.fieldBounds = fieldSize.getBoundsAtPosition(center);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        if (fieldBounds != null)
            return fieldBounds.inflate(20);

        return new AxisAlignedBB(worldPosition).inflate(20);
    }

    public LazyOptional<IMiniaturizationField> getField() {
        return this.fieldCap.cast();
    }
}
