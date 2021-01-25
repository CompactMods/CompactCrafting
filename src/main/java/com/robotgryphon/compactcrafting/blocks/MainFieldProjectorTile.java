package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainFieldProjectorTile extends FieldProjectorTile implements ITickableTileEntity {

    private EnumCraftingState craftingState = EnumCraftingState.NOT_MATCHED;
    private FieldProjection field = null;
    private MiniaturizationRecipe currentRecipe = null;

    @Override
    public void remove() {
        super.remove();

        // Invalidate field
        invalidateField();
    }

    @Override
    public void tick() {
        if (this.currentRecipe != null)
            tickCrafting();
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    public void doFieldCheck() {

        if (this.field != null)
            return;

        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(world, this.pos);
        if (field.isPresent()) {
            this.field = field.get();

            if (world != null && !world.isRemote) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
                data.ACTIVE_FIELDS.put(this.field.getCenterPosition(), ProjectorFieldData.fromInstance(this.field));
                data.markDirty();
            }
        } else {
            this.invalidateField();
        }

    }

    public void invalidateField() {
        if(field == null)
            return;

        if (world != null && !world.isRemote) {
            BlockPos center = this.field.getCenterPosition();
            ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
            data.unregister(center);
        }

        this.field = null;
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if (this.field == null)
            return;

        if (this.world == null)
            return;

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

        /*
         * Dry run - we have the data from the field on what's filled and how large
         * the area is. Run through the recipe list and filter based on that, so
         * we remove all the recipes that are definitely larger than the currently
         * filled space.
         */
        Set<MiniaturizationRecipe> recipes = world.getRecipeManager()
                .getRecipesForType(Registration.MINIATURIZATION_RECIPE_TYPE)
                .stream().map(r -> (MiniaturizationRecipe) r)
                .filter(recipe -> recipe.fitsInDimensions(fieldBlocks.getFilledBounds()))
                .collect(Collectors.toSet());

        /*
         * All the recipes we have registered won't fit in the filled bounds -
         * blocks were placed in a larger space than the max recipe size
         */
        if (recipes.isEmpty()) {
            this.currentRecipe = null;
            updateCraftingState(EnumCraftingState.NOT_MATCHED);
            return;
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        MiniaturizationRecipe matchedRecipe = null;
        for (MiniaturizationRecipe recipe : recipes) {
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

    @Override
    public Optional<BlockPos> getMainProjectorPosition() {
        return Optional.ofNullable(pos);
    }

    @Override
    public Optional<MainFieldProjectorTile> getMainProjectorTile() {
        return Optional.of(this);
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
