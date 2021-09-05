package dev.compactmods.crafting.proxies.block;

import javax.annotation.Nullable;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RescanFieldProxyBlock extends FieldProxyBlock {

    public RescanFieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RescanFieldProxyEntity();
    }

    @Override
    public void neighborChanged(BlockState thisState, World level, BlockPos thisPos, Block changedBlock, BlockPos changedPos, boolean _b) {
        if (!level.isClientSide) {
            BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(thisPos);

            doRescanRedstone(level, thisPos, tile);
        }
    }

    private void doRescanRedstone(World level, BlockPos thisPos, BaseFieldProxyEntity tile) {
        if (level.hasNeighborSignal(thisPos)) {
            // call recipe scan

            if (tile != null) {
                tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                        .ifPresent(IMiniaturizationField::fieldContentsChanged);
            }
        }
    }
}
