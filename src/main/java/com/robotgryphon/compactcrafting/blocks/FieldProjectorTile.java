package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.config.ClientConfig;
import com.robotgryphon.compactcrafting.core.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
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

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldProjectorTile extends TileEntity implements ITickableTileEntity {

    private EnumCraftingState craftingState = EnumCraftingState.NOT_MATCHED;
    private FieldProjection field = null;
    private MiniaturizationRecipe currentRecipe = null;
    private int fieldCheckTimeout = 0;

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Color getProjectionColor(EnumProjectorColorType type) {
        Color base = ClientConfig.projectorColor;
        new Color(255, 106, 0, 100);
        // Color base = Color.red.brighter();
        int red = base.getRed();
        int green = base.getGreen();
        int blue = base.getBlue();

        switch (type) {
            case FIELD:
            case SCAN_LINE:
                return new Color(red, green, blue, 100);

            case PROJECTOR_FACE:
                // return new Color(Color.cyan.getRed(), Color.cyan.getGreen(), Color.cyan.getBlue(), 100);
                return new Color(red, green, blue, 250);
        }

        return Color.WHITE;
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
        if (field == null)
            return Optional.empty();

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

        if (this.field != null) {
            BlockPos north = field.getProjectorInDirection(Direction.NORTH);

            return Optional.of(north);
        }

        return Optional.empty();
    }

    public void invalidateField() {
        if (world == null || world.isRemote)
            return;

        if (field != null) {
            BlockPos center = this.field.getCenterPosition();
            ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
            data.ACTIVE_FIELDS.remove(center);
            data.markDirty();

            this.field = null;
            this.fieldCheckTimeout = 20;
        }
    }

    @Override
    public void tick() {
        // TODO: Do this in the field projector placement method instead
        // If we don't have a valid field, search again
        if (field == null) {
            if (fieldCheckTimeout > 0) {
                fieldCheckTimeout--;
                return;
            }

            // Check timeout has elapsed, run field scan
            doFieldCheck();

            // If we still don't have a valid field, get out of tick logic
            if (field == null)
                return;
        }

        if (this.currentRecipe != null)
            tickCrafting();
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    private void doFieldCheck() {


        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(world, this.pos);
        if (field.isPresent()) {
            this.field = field.get();

            if (world != null && !world.isRemote) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
                data.ACTIVE_FIELDS.put(this.field.getCenterPosition(), ProjectorFieldData.fromInstance(this.field));
                data.markDirty();
            }

            return;
        }

        this.field = null;

        // If we didn't find a valid field, restart timer and keep looking
        fieldCheckTimeout = 20;
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if (this.field == null)
            return;

        if (this.world == null)
            return;

        FieldProjectionSize size = field.getFieldSize();

        // Only the primary projector needs to worry about the recipe scan
        if (!isMainProjector()) {
            Optional<BlockPos> center = ProjectorHelper.getCenterForSize(world, pos, size);
            if (!center.isPresent())
                return;

            BlockPos masterPos = ProjectorHelper.getProjectorLocationForDirection(world, center.get(), Direction.NORTH, size);

            FieldProjectorTile masterTile = (FieldProjectorTile) world.getTileEntity(masterPos);
            if (masterTile == null)
                return;

            masterTile.doRecipeScan();
            return;
        }

        List<MiniaturizationRecipe> recipes = world.getRecipeManager()
                .getRecipesForType(Registration.MINIATURIZATION_RECIPE_TYPE)
                .stream().map(r -> (MiniaturizationRecipe) r)
                .collect(Collectors.toList());

        // If there are no registered recipes, then we obv can't match anything - exit early
        if (recipes.isEmpty()) {
            this.currentRecipe = null;
            updateCraftingState(EnumCraftingState.NOT_MATCHED);
            return;
        }

        AxisAlignedBB fieldBounds = field.getBounds();

        MiniaturizationFieldBlockData fieldBlocks = MiniaturizationFieldBlockData.getFromField(world, fieldBounds);

        // If no positions filled, exit early
        if (fieldBlocks.getNumberFilledBlocks() == 0) {
            this.currentRecipe = null;
            updateCraftingState(EnumCraftingState.NOT_MATCHED);
            return;
        }

        // ===========================================================================================================
        //   RECIPE BEGIN
        // ===========================================================================================================
        Set<MiniaturizationRecipe> recipesBoundFitted = recipes
                .stream()
                .filter(recipe -> recipe.fitsInDimensions(fieldBlocks.getFilledBounds()))
                .collect(Collectors.toSet());

        // All the recipes we have registered won't fit in the filled bounds -
        // blocks were placed in a larger space than the max recipe size
        if (recipesBoundFitted.size() == 0) {
            this.currentRecipe = null;
            updateCraftingState(EnumCraftingState.NOT_MATCHED);
            return;
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        MiniaturizationRecipe matchedRecipe = null;
        for (MiniaturizationRecipe recipe : recipesBoundFitted) {
            boolean recipeMatches = recipe.matches(world, field.getFieldSize(), fieldBlocks);
            if (!recipeMatches)
                continue;

            matchedRecipe = recipe;
            updateCraftingState(EnumCraftingState.MATCHED);
            break;
        }

        this.currentRecipe = matchedRecipe;
    }

    private void tickCrafting() {
        if (this.field != null) {
            AxisAlignedBB fieldBounds = field.getBounds();

            // Get out, client worlds
            if (world == null || world.isRemote())
                return;

            // We grow the bounds check here a little to support patterns that are exactly the size of the field
            List<ItemEntity> catalystEntities = getCatalystsInField(fieldBounds.grow(0.25), currentRecipe.getCatalyst().getItem());
            if (catalystEntities.size() > 0) {
                // We dropped a catalyst item in
                // At this point, we had a valid recipe and a valid catalyst entity
                // Start crafting
                switch (craftingState) {
                    case MATCHED:
                        updateCraftingState(EnumCraftingState.CRAFTING);

                        // We know the "recipe" in the field is an exact match already, so wipe the field
                        field.clearBlocks(world);

                        CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                        BlockPos centerField = field.getCenterPosition();
                        world.setBlockState(centerField, Registration.FIELD_CRAFTING_PREVIEW_BLOCK.get().getDefaultState());
                        FieldCraftingPreviewTile tile = (FieldCraftingPreviewTile) world.getTileEntity(centerField);
                        if (tile != null)
                            tile.setMasterProjector(this);

                        break;

                    case CRAFTING:
                        break;

                    case DONE:
                        // We aren't crafting any more - recipe complete, reset for next one
                        updateCraftingState(EnumCraftingState.NOT_MATCHED);
                        this.currentRecipe = null;
                        break;
                }
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
        if (this.field != null) {
            return field.getBounds().grow(10);
        }

        return super.getRenderBoundingBox();
    }

    public Optional<FieldProjection> getField() {
        return Optional.ofNullable(this.field);
    }

    public Optional<MiniaturizationRecipe> getCurrentRecipe() {
        return Optional.ofNullable(this.currentRecipe);
    }

    public void updateCraftingState(EnumCraftingState state) {
        this.craftingState = state;
    }

    public EnumCraftingState getCraftingState() {
        return this.craftingState;
    }
}
