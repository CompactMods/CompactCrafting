package dev.compactmods.crafting.field.block;

import javax.annotation.Nullable;
import dev.compactmods.crafting.field.tile.FieldCraftingPreviewTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class FieldCraftingPreviewBlock extends Block {
    public FieldCraftingPreviewBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
//        if(world instanceof ICapabilityProvider) {
//            ICapabilityProvider cap = ((ICapabilityProvider) world);
//            final LazyOptional<IActiveWorldFields> fields = cap.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);
//
//            fields.ifPresent(f -> f.get());
//        }
        return new FieldCraftingPreviewTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return false;
    }
}
