package dev.compactmods.crafting.proxies.block;

import org.jetbrains.annotations.Nullable;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FieldProxyBlock extends Block {
    public static IntegerProperty SIGNAL = BlockStateProperties.POWER;

    private static final VoxelShape BASE = Shapes.box(0, 0, 0, 1, 6 / 16d, 1);

    private static final VoxelShape POLE = Shapes.box(7 / 16d, 6 / 16d, 7 / 16d, 9 / 16d, 12 / 16d, 9 / 16d);

    public FieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter levelReader, BlockPos pos, CollisionContext ctx) {
        return Shapes.or(BASE, POLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIGNAL);
    }

    // TODO - Proxy pick block
//    @Override
//    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
//        ItemStack stack = new ItemStack(this);
//
//        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(pos);
//        if (tile != null) {
//            tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
//                    .ifPresent(field -> {
//                        CompoundTag fieldInfo = stack.getOrCreateTagElement("field");
//                        fieldInfo.put("center", NbtUtils.writeBlockPos(field.getCenter()));
//                    });
//        }
//
//        return stack;
//    }

    @Override
    public void setPlacedBy(Level level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(placedAt);

        if (stack.hasTag()) {
            CompoundTag nbt = stack.getTag();

            if (nbt != null && nbt.contains("field")) {
                CompoundTag fieldData = nbt.getCompound("field");
                if (fieldData.contains("center")) {
                    BlockPos center = NbtUtils.readBlockPos(fieldData.getCompound("center"));

                    if (tile != null)
                        tile.updateField(center);
                }
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return true;
    }
}
