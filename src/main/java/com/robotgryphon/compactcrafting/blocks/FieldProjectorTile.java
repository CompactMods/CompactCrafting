package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FieldProjectorTile extends TileEntity implements ITickableTileEntity {

    private boolean isCrafting = false;
    private Optional<FieldProjection> field = Optional.empty();
    private MiniaturizationRecipe currentRecipe = null;
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

        int offset = (size.getProjectorDistance() * 2) + 1;
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
        if(world == null || world.isRemote)
            return;

        if(this.field.isPresent()) {
            BlockPos center = this.field.get().getCenterPosition();
            ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
            data.ACTIVE_FIELDS.remove(center);
            data.markDirty();

            this.field = Optional.empty();
            this.fieldCheckTimeout = 20;
        }
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

        if(this.currentRecipe != null)
            tickCrafting();
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    private void doFieldCheck() {


        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(world, this.pos);
        if (field.isPresent()) {
            this.field = field;
            FieldProjection fp = field.get();

            if(world != null && !world.isRemote) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
                data.ACTIVE_FIELDS.put(fp.getCenterPosition(), ProjectorFieldData.fromInstance(fp));
                data.markDirty();
            }

            return;
        }

        this.field = Optional.empty();

        // If we didn't find a valid field, restart timer and keep looking
        fieldCheckTimeout = 20;
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if(!this.field.isPresent())
            return;

        FieldProjection fp = this.field.get();
        FieldProjectionSize size = fp.getFieldSize();

        // Only the primary projector needs to worry about the recipe scan
        if(!isMainProjector())
        {
            Optional<BlockPos> center = ProjectorHelper.getCenterForSize(world, pos, size);
            BlockPos masterPos = ProjectorHelper.getProjectorLocationForDirection(world, center.get(), Direction.NORTH, size);

            FieldProjectorTile masterTile = (FieldProjectorTile) world.getTileEntity(masterPos);
            masterTile.doRecipeScan();
            return;
        }

        AxisAlignedBB fieldBounds = fp.getBounds();

        Collection<RegistryObject<MiniaturizationRecipe>> entries = Registration.MINIATURIZATION_RECIPES.getEntries();
        if (entries.isEmpty())
            return;

        FieldProjection field = this.field.get();

        Stream<MiniaturizationRecipe> matchedRecipes = entries
                .stream()
                .map(RegistryObject::get)
                .filter(recipe -> recipe.fitsInFieldSize(size))
                .filter(recipe -> recipe.matches(world, size, fieldBounds));

        Optional<MiniaturizationRecipe> matched = matchedRecipes.findFirst();

        this.currentRecipe = matched.orElse(null);
    }

    private void tickCrafting() {
        if (this.field.isPresent()) {
            FieldProjection fieldProjection = this.field.get();
            AxisAlignedBB fieldBounds = fieldProjection.getBounds();

            // Get out, client worlds
            if (world == null || world.isRemote())
                return;

            List<ItemEntity> catalystEntities = getCatalystsInField(fieldBounds, currentRecipe.catalyst);
            if (catalystEntities.size() > 0) {
                // We dropped a catalyst item in
                // At this point, we had a valid recipe and a valid catalyst entity
                // Start crafting
                this.isCrafting = true;

                // We know the "recipe" in the field is an exact match already, so wipe the field
                fieldProjection.clearBlocks(world);

                CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                BlockPos fieldCenter = field.get().getCenterPosition();
                for (ItemStack is : currentRecipe.getOutputs()) {
                    ItemEntity itemEntity = new ItemEntity(world, fieldCenter.getX() + 0.5f, fieldCenter.getY() + 0.5f, fieldCenter.getZ() + 0.5f, is);
                    world.addEntity(itemEntity);
                }

                // We aren't crafting any more - recipe complete, reset for next one
                this.isCrafting = false;
                this.currentRecipe = null;
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
            return fp.getBounds().grow(10);
        }

        return super.getRenderBoundingBox();
    }

    public Optional<FieldProjection> getField() {
        return this.field;
    }
}
