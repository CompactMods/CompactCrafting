package com.robotgryphon.compactcrafting.blocks.tiles;

import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class FieldProjectorTile extends TileEntity implements ITickableTileEntity {

    private boolean isCrafting = false;
    private Optional<FieldProjection> field = Optional.empty();
    private int fieldCheckTimeout = 0;

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    @Override
    public void remove() {
        super.remove();

        // Invalidate field
        invalidateField();
    }

    /**
     * Gets the location of the opposite field projector.
     *
     * @return
     */
    public Optional<BlockPos> getOppositeProjector() {
        if (!field.isPresent())
            return Optional.empty();

        FieldProjection field = this.field.get();
        FieldProjectionSize size = field.getFieldSize();

        int offset = (size.getOffset() * 2) + 1;
        BlockPos opposite = getPos().offset(getFacing(), offset);

        return Optional.of(opposite);
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        Direction facing = bs.get(FieldProjectorBlock.FACING);

        return facing;
    }

    public Direction getProjectorSide() {
        Direction facing = getFacing();
        return facing.getOpposite();
    }

    public boolean isMainProjector() {
        Direction side = getProjectorSide();

        // We're the main projector if we're to the NORTH
        return side == Direction.NORTH;
    }

    public Optional<BlockPos> getMainProjectorPosition() {
        if (this.isMainProjector())
            return Optional.of(this.pos);

        if (this.field.isPresent()) {
            FieldProjection fp = this.field.get();
            BlockPos north = fp.getProjectorInDirection(Direction.NORTH);

            return Optional.of(north);
        }

        return Optional.empty();
    }

    public void invalidateField() {
        this.field = Optional.empty();
        this.fieldCheckTimeout = 20;
    }

    @Override
    public void tick() {
        // If we don't have a valid field, search again
        if (!field.isPresent()) {
            if (fieldCheckTimeout > 0) {
                fieldCheckTimeout--;
                return;
            }

            // Check timeout has elapsed, run field scan
            doFieldCheck();

            // If we still don't have a valid field, get out of tick logic
            if (!field.isPresent())
                return;
        }

        tickCrafting();
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    private void doFieldCheck() {
        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(world, this.pos);
        if (field.isPresent()) {
            this.field = field;
            return;
        }

        this.field = Optional.empty();

        // If we didn't find a valid field, restart timer and keep looking
        fieldCheckTimeout = 20;
    }

    private void beginCraft() {
        this.isCrafting = true;
    }

    private void tickCrafting() {
        FieldProjection fp = this.field.get();
        Optional<AxisAlignedBB> fieldBounds = fp.getBounds();

        List<ItemEntity> enderPearls = getCatalystsInField(fieldBounds.get(), Items.ENDER_PEARL);
        if(enderPearls.size() > 0) {
            // We dropped an ender pearl in - are there any blocks in the field?
            // If so, we'll need to check the recipes -- but for now we just use it to
            // not delete the item if there's nothing in here
            if (CraftingHelper.hasBlocksInField(world, fieldBounds.get())) {
                this.isCrafting = true;

                if(!world.isRemote()) {
                    CraftingHelper.deleteCraftingBlocks(world, fieldBounds.get());
                    CraftingHelper.consumeCatalystItem(enderPearls.get(0), 1);
                }

                this.isCrafting = false;
            }
        }
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
                .map(ItemEntity::getItem)
                .map(ItemStack::getCount)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        if (this.field.isPresent()) {
            FieldProjection fp = this.field.get();
            if (!fp.getBounds().isPresent())
                return super.getRenderBoundingBox();

            return fp.getBounds().get().grow(10);
        }

        return super.getRenderBoundingBox();
    }

    public Optional<FieldProjection> getField() {
        return this.field;
    }

    /**
     * Called whenever a nearby block is changed near the field.
     * @param pos The position a block was updated at.
     */
    public void handleNearbyBlockUpdate(BlockPos pos, BlockUpdateType updateType) {
        if(updateType == BlockUpdateType.UNKNOWN)
            return;

        doFieldCheck();
    }
}
