package com.robotgryphon.compactcrafting.proxies.block;

import javax.annotation.Nullable;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class FieldProxyBlock extends Block {
    public static IntegerProperty SIGNAL = BlockStateProperties.POWER;

    private static final VoxelShape BASE = VoxelShapes.box(0, 0, 0, 1, 6 / 16d, 1);

    private static final VoxelShape POLE = VoxelShapes.box(7 / 16d, 6 / 16d, 7 / 16d, 9 / 16d, 12 / 16d, 9 / 16d);

    public FieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader levelReader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.or(BASE, POLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SIGNAL);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader level, BlockPos pos, PlayerEntity player) {
        ItemStack stack = new ItemStack(this);

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(pos);
        if (tile != null) {
            tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                    .ifPresent(field -> {
                        CompoundNBT fieldInfo = stack.getOrCreateTagElement("field");
                        fieldInfo.put("center", NBTUtil.writeBlockPos(field.getCenter()));
                    });
        }

        return stack;
    }

    @Override
    public void setPlacedBy(World level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(placedAt);

        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();

            if (nbt != null && nbt.contains("field")) {
                CompoundNBT fieldData = nbt.getCompound("field");
                if (fieldData.contains("center")) {
                    BlockPos center = NBTUtil.readBlockPos(fieldData.getCompound("center"));

                    if (tile != null)
                        tile.updateField(center);
                }
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }
}
