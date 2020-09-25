package com.robotgryphon.compactcrafting.blocks.tiles;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.core.FieldProjectionSize;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FieldProjectorTile extends TileEntity implements ITickableTileEntity {

    private FieldProjectionSize projectionSize = FieldProjectionSize.MEDIUM;

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Optional<BlockPos> getCenter() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);


        Optional<BlockPos> location = getOppositeProjector();
        if (location.isPresent()) {
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
     *
     * @return
     */
    public Optional<BlockPos> getOppositeProjector() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);

        for (int i = 1; i <= 13; i++) {
            // check block exists in offset position
            BlockPos offsetInDirection = pos.offset(facing, i);
            BlockState stateInPos = world.getBlockState(offsetInDirection);

            if (stateInPos.getBlock() instanceof FieldProjectorBlock) {
                // We have a field projector
                Direction otherFpDirection = stateInPos.get(FieldProjectorBlock.FACING);
                if (otherFpDirection == facing.getOpposite())
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

    public FieldProjectionSize getProjectionSize() {
        // TODO: Make this different depending on the spacing of the field projectors
        return FieldProjectionSize.MEDIUM;
    }

    @Override
    public void tick() {
        if (!isMainProjector())
            return;

        Optional<AxisAlignedBB> fieldBounds = getFieldBounds();
        if(fieldBounds.isPresent()) {
            // TODO: Check crafting state of projection field, update that state instead of doing manual work here
            boolean hasCatalyst = hasCatalystInBounds(fieldBounds.get(), Items.ENDER_PEARL);

            if(hasCatalyst) {
                List<ItemEntity> enderPearls = getCatalystsInField(fieldBounds.get(), Items.ENDER_PEARL);

                // We dropped an ender pearl in - are there any blocks in the field?
                // If so, we'll need to check the recipes -- but for now we just use it to
                // not delete the item if there's nothing in here
                if(CraftingHelper.hasBlocksInField(world, fieldBounds.get())) {
                    CraftingHelper.consumeCatalystItem(enderPearls.get(0), 1);
                    CraftingHelper.deleteCraftingBlocks(world, fieldBounds.get());
                }
            }
        }
    }

    private Optional<AxisAlignedBB> getFieldBounds() {
        Optional<BlockPos> center = getCenter();

        // No center area - no other projector present
        if (!center.isPresent())
            return Optional.empty();

        BlockPos centerPos = center.get();
        FieldProjectionSize size = getProjectionSize();
        AxisAlignedBB bounds = new AxisAlignedBB(centerPos).grow(size.getSize());

        return Optional.of(bounds);
    }

    private List<ItemEntity> getCatalystsInField(AxisAlignedBB fieldBounds, Item itemFilter) {
        List<ItemEntity> itemsInRange = world.getEntitiesWithinAABB(ItemEntity.class, fieldBounds);
        return itemsInRange.stream()
                .filter(ise -> ise.getItem().getItem() == itemFilter)
                .collect(Collectors.toList());
    }

    private boolean hasCatalystInBounds(AxisAlignedBB bounds, Item itemFilter) {
        try {
            List<ItemEntity> itemsInRange = getCatalystsInField(bounds, itemFilter);
            int matchedCatalysts = collectItems(itemsInRange);

            return matchedCatalysts > 0;
        } catch (NullPointerException npe) {

        }

        return false;
    }

    private int collectItems(List<ItemEntity> itemsInRange) {
        return itemsInRange.stream()
                // .filter(ise -> ise.getItem().getItem() == item)
                .map(ItemEntity::getItem)
                .map(ItemStack::getCount)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Optional<AxisAlignedBB> field = this.getFieldBounds();
        if(field.isPresent()) {
            return field.get().grow(10);
        }

        return super.getRenderBoundingBox();
    }
}
