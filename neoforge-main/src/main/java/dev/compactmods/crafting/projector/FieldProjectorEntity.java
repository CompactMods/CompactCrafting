package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FieldProjectorEntity extends BlockEntity {

    public FieldProjectorEntity(BlockPos pos, BlockState state) {
        super(CCBlocks.FIELD_PROJECTOR_TILE.get(), pos, state);
    }

    public Direction getProjectorSide() {
        return getBlockState().getValue(FieldProjectorBlock.FACING).getOpposite();
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
