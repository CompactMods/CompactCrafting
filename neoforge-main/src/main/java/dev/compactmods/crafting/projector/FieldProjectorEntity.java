package dev.compactmods.crafting.projector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class FieldProjectorEntity extends BlockEntity {

    public FieldProjectorEntity(BlockPos pos, BlockState state) {
        super(FieldProjectorsCommon.FIELD_PROJECTOR_TILE.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
//        ActiveProjectorInfo.from(pos, state).ifPresent(this::turnOffProjector);
    }

//    private void turnOffProjector(ActiveProjectorInfo projector) {
//        if (!projector.isActive())
//            return;
//
//        final var fieldCenter = projector.getTargetPosition()
//                .orElseThrow();
//
//        projector.target()
//                .getProjectors(fieldCenter)
//                .disableAll(level);
//
//        // Remove projectors registration - this will also update clients
//        Objects.requireNonNull(level);
//        level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
//            if (fields.hasActiveField(fieldCenter)) {
//                final var field = fields.get(fieldCenter).orElse(null);
//                if (field == null) return;
//
//                if (field.enabled()) {
//                    fields.unregisterField(fieldCenter);
//                    field.handleDestabilize();
//                    field.dispose();
//                }
//            }
//        });
//    }

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
