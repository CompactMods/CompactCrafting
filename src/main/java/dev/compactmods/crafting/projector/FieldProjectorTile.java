package dev.compactmods.crafting.projector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import net.minecraft.block.BlockState;
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

public class FieldProjectorTile extends TileEntity {

    protected LazyOptional<IMiniaturizationField> fieldCap = LazyOptional.empty();
    protected LazyOptional<IActiveWorldFields> levelFields = LazyOptional.empty();

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public FieldProjectorTile(IBlockReader level, BlockState state) {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Direction getProjectorSide() {
        return getBlockState().getValue(FieldProjectorBlock.FACING).getOpposite();
    }

    @Override
    public void setLevelAndPosition(World level, BlockPos pos) {
        super.setLevelAndPosition(level, pos);
        this.levelFields = level.getCapability(CapabilityActiveWorldFields.FIELDS);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null) {
            if(level.isClientSide) {
                loadFieldFromState();
            } else {
                MinecraftServer server = level.getServer();
                if(server != null)
                    server.tell(new TickDelayedTask(0, this::loadFieldFromState));
            }
        }
    }

    private void loadFieldFromState() {
        this.fieldCap = levelFields.lazyMap(fields -> {
            BlockState state = getBlockState();
            return fields.getLazy(FieldProjectorBlock.getFieldCenter(state, worldPosition));
        }).orElse(LazyOptional.empty());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityActiveWorldFields.FIELDS)
            return levelFields.cast();

        if (cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return fieldCap.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        return fieldCap
                .map(f -> f.getBounds().inflate(20))
                .orElse(new AxisAlignedBB(worldPosition).inflate(20));
    }

    public LazyOptional<IMiniaturizationField> getField() {
        return this.fieldCap.cast();
    }

    public void setFieldRef(LazyOptional<IMiniaturizationField> fieldRef) {
        this.fieldCap = fieldRef;
        setChanged();
    }
}
