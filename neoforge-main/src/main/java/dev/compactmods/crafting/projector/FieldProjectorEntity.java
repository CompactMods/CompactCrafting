package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.data.CCAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class FieldProjectorEntity extends BlockEntity {

    public FieldProjectorEntity(BlockPos pos, BlockState state) {
        super(CCBlocks.FIELD_PROJECTOR_TILE.get(), pos, state);
    }

    public Direction getProjectorSide() {
        return getBlockState().getValue(FieldProjectorBlock.FACING).getOpposite();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        Objects.requireNonNull(level);

        if (FieldProjectorBlock.isActive(state)) {
            final var fieldSize = state.getValue(FieldProjectorBlock.SIZE);
            final var fieldCenter = FieldProjectorBlock.getFieldCenter(state, pos);

            fieldSize.getProjectorLocations(fieldCenter).forEach(proj -> FieldProjectorBlock.deactivateProjector(level, proj));

            // Remove field registration - this will also update clients
            level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                if (fields.hasActiveField(fieldCenter)) {
                    final IMiniaturizationField field = fields.get(fieldCenter).orElse(null);
                    if (field == null) return;

                    if (field.enabled()) {
                        fields.unregisterField(fieldCenter);
                        field.handleDestabilize();
                        field.dispose();
                    }
                }
            });
        }
    }

    //    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//        if (cap == CCCapabilities.FIELDS)
//            return levelFields.cast();
//
//        if (cap == CCCapabilities.MINIATURIZATION_FIELD)
//            return fieldCap.cast();
//
//        return super.getCapability(cap, side);
//    }
}
