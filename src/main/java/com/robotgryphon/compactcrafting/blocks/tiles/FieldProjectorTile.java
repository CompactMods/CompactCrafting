package com.robotgryphon.compactcrafting.blocks.tiles;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;

public class FieldProjectorTile extends TileEntity implements ITickableTileEntity  {

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Optional<BlockPos> getCenter() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);


        Optional<BlockPos> location = getOppositeProjector();
        if(location.isPresent()) {
            BlockPos opp = location.get();
            int centerOffset = opp.manhattanDistance(pos);

            BlockPos center = pos.offset(facing, centerOffset / 2);
            return Optional.of(center);
        } else {
            // No center -- no found opposite
            return Optional.empty();
        }
    }

    /**
     * Gets the location of the opposite field projector.
     * @return
     */
    public Optional<BlockPos> getOppositeProjector() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);

        for(int i = 1; i <= 13; i++) {
            // check block exists in offset position
            BlockPos offsetInDirection = pos.offset(facing, i);
            BlockState stateInPos = world.getBlockState(offsetInDirection);

            if(stateInPos.getBlock() instanceof FieldProjectorBlock) {
                // We have a field projector
                Direction otherFpDirection = stateInPos.get(FieldProjectorBlock.FACING);
                if(otherFpDirection == facing.getOpposite())
                    return Optional.of(offsetInDirection);
            }
        }

        return Optional.empty();
    }

    public Direction getProjectorSide() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);

        return facing.getOpposite();
    }

    public boolean isMainProjector() {
        Direction side = getProjectorSide();

        // We're the main projector if we're to the NORTH
        return side == Direction.NORTH;
    }

    @Override
    public void tick() {
        if(!isMainProjector())
            return;

        Optional<BlockPos> center = getCenter();
        if(center.isPresent()) {
            BlockPos c = center.get();
            world.addParticle(ParticleTypes.SMOKE, c.getX() + 0.5f, c.getY() + 0.5f, c.getZ() + 0.5f, 0.0D, 0.0D, 0.0D);
        }

        searchForCatalyst();
    }

    private void searchForCatalyst() {
        Optional<BlockPos> center = getCenter();

        // No center area - no other projector present
        if(!center.isPresent())
            return;

        BlockPos centerPos = center.get();
        AxisAlignedBB itemSuckBounds = new AxisAlignedBB(centerPos).grow(2);

        try {
            List<ItemEntity> itemsInRange = world.getEntitiesWithinAABB(ItemEntity.class, itemSuckBounds);
            collectItems(centerPos, itemsInRange);
        }

        catch(NullPointerException npe) {

        }
    }

    private void collectItems(BlockPos center, List<ItemEntity> itemsInRange) {
        itemsInRange.forEach(item -> {
            if(item.getItem().getItem() == Items.ENDER_PEARL) {
                PlayerEntity closestPlayer = world.getClosestPlayer(center.getX(), center.getY(), center.getZ(), 5, false);

                item.remove(false);

                ItemStack output = new ItemStack(Items.DIAMOND, 1);
                closestPlayer.addItemStackToInventory(output);
            }
        });
    }
}
