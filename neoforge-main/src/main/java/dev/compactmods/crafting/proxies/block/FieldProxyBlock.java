package dev.compactmods.crafting.proxies.block;

import dev.compactmods.crafting.CompactCrafting;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.core.CCDataComponents;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        this.registerDefaultState(this.stateDefinition.any().setValue(SIGNAL, 0));
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

    @Override
    public void setPlacedBy(Level level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(placedAt);

        var fieldCenter = stack.get(CCDataComponents.FIELD_CENTER.get());
        if (fieldCenter != null && tile != null) {
            tile.updateField(fieldCenter.center());
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return true;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.isClientSide && !state.is(newState.getBlock())) {
            ClientPacketHandler.removeProxyData(pos);
            if(!FMLLoader.isProduction())CompactCrafting.LOGGER.debug("Removed proxy data for {}", pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}