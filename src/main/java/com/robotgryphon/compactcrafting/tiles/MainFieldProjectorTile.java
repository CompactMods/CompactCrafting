package com.robotgryphon.compactcrafting.tiles;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.network.FieldActivatedPacket;
import com.robotgryphon.compactcrafting.network.FieldDeactivatedPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainFieldProjectorTile extends FieldProjectorTile implements ITickableTileEntity {
    private EnumCraftingState craftingState = EnumCraftingState.NOT_MATCHED;
    private FieldProjection field;
    private ResourceLocation recipeId;
    private MiniaturizationRecipe currentRecipe;

    public MainFieldProjectorTile() {
        super(Registration.MAIN_FIELD_PROJECTOR_TILE.get());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // Invalidate field
        invalidateField();
    }

    @Override
    public void tick() {
        if (this.currentRecipe == null) {
            if (this.recipeId == null) {
                // Handled by the projector block scheduling block ticks when blocks are placed
                // doRecipeScan();
            } else {
                // If the current recipe is null but not the id, the world was null when loading the recipe data
                loadRecipe();
            }
        } else {
            tickCrafting();
        }
    }

    /**
     * Invalidates the current field projection and attempts to rebuild it from this position as an initial.
     */
    public void doFieldCheck() {
        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(level, this.worldPosition);
        if (field.isPresent()) {
            this.field = field.get();

            if (level != null && !level.isClientSide) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) level);
                data.getActiveFields().put(this.field.getCenterPosition(), ProjectorFieldData.fromInstance(this.field));
                data.setDirty();

                PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                        .with(() -> level.getChunkAt(this.worldPosition));

                NetworkHandler.MAIN_CHANNEL
                        .send(trk, new FieldActivatedPacket(this.field.getCenterPosition(), this.field.getFieldSize()));
            }
        } else {
            this.invalidateField();
        }

    }

    public void invalidateField() {
        if (field == null)
            return;

        if (level != null && !level.isClientSide) {
            BlockPos center = this.field.getCenterPosition();
            FieldProjectionSize size = this.field.getFieldSize();

            ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) level);
            data.unregister(center);

            PacketDistributor.PacketTarget trk = PacketDistributor.TRACKING_CHUNK
                    .with(() -> level.getChunkAt(this.worldPosition));

            NetworkHandler.MAIN_CHANNEL
                    .send(trk, new FieldDeactivatedPacket(center, size));
        }

        this.field = null;
        this.setChanged();
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        // Requires field and server-side
        if (this.field == null || this.level == null || this.level.isClientSide)
            return;

        AxisAlignedBB fieldBounds = field.getBounds();

        MiniaturizationFieldBlockData fieldBlocks = MiniaturizationFieldBlockData.getFromField(level, fieldBounds);

        // If no positions filled, exit early
        if (fieldBlocks.getFilledBlocks().isEmpty()) {
            return; // Don't set NOT_MATCHED here since an empty field means keep the last state (e.g. DONE)
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
        Set<MiniaturizationRecipe> recipes = RecipeHelper.getLoadedRecipesThatFitField(this.level, fieldBlocks);

        /*
         * All the recipes we have registered won't fit in the filled bounds -
         * blocks were placed in a larger space than the max recipe size
         */
        if (recipes.isEmpty()) {
            updateCraftingState(EnumCraftingState.NOT_MATCHED);
            return;
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        for (MiniaturizationRecipe recipe : recipes) {
            boolean recipeMatches = recipe.matches(level, field.getFieldSize(), fieldBlocks);
            if (recipeMatches) {
                updateCraftingState(EnumCraftingState.MATCHED);
                setRecipe(recipe);
                return;
            }
        }

        // No recipes matched - set to NOT_MATCHED
        updateCraftingState(EnumCraftingState.NOT_MATCHED);
    }

    public void setFieldInfo(FieldProjection field) {
        this.field = field;
        this.setChanged();
    }

    private void tickCrafting() {
        // Requires field and to be server-side
        if (this.field == null || this.level == null || this.level.isClientSide)
            return;

        AxisAlignedBB fieldBounds = field.getBounds();

        // We grow the bounds check here a little to support patterns that are exactly the size of the field
        ItemStack catalyst = currentRecipe.getCatalyst();
        List<ItemEntity> catalystEntities = getCatalystsInField(fieldBounds.inflate(0.5D), catalyst.getItem());
        if (!catalystEntities.isEmpty()) {
            // We dropped a catalyst item in
            // At this point, we had a valid recipe and a valid catalyst entity
            // Start crafting
            switch (craftingState) {
                case MATCHED:
                    updateCraftingState(EnumCraftingState.CRAFTING);

                    // We know the "recipe" in the field is an exact match already, so wipe the field
                    field.clearBlocks(level);

                   if (!CraftingHelper.consumeCatalystItem(catalystEntities.get(0), catalyst.getCount()))
                       break;

                    BlockPos centerField = field.getCenterPosition();
                    level.setBlockAndUpdate(centerField, Registration.FIELD_CRAFTING_PREVIEW_BLOCK.get().defaultBlockState());
                    FieldCraftingPreviewTile tile = (FieldCraftingPreviewTile) level.getBlockEntity(centerField);
                    if (tile != null)
                        tile.setMasterProjector(this);

                    break;

                case CRAFTING:
                case DONE: // For DONE, we will keep it DONE until a new recipe is attempted to be matched. doRecipeScan() will handle this
                    break;
            }
        }
    }

    private void loadRecipe() {
        // The world can be null when loading the tile entity
        if (this.level == null)
            return;

        Optional<? extends IRecipe<?>> foundRecipe = this.level.getRecipeManager().byKey(this.recipeId);
        if (foundRecipe.isPresent()) {
            this.currentRecipe = (MiniaturizationRecipe) foundRecipe.get();
        } else {
            // The recipe has become invalid
            this.recipeId = null;
        }
    }

    private List<ItemEntity> getCatalystsInField(AxisAlignedBB fieldBounds, Item itemFilter) {
        List<ItemEntity> itemsInRange = level.getEntitiesOfClass(ItemEntity.class, fieldBounds);
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
        // Check - if we have a valid field use the entire field including projectors
        // Otherwise just use the super implementation
        if (this.field != null) {
            return field.getBounds().inflate(field.getFieldSize().getProjectorOffset());
        }

        return super.getRenderBoundingBox();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        CompoundNBT nbt = super.save(compound);

        if (this.craftingState != null)
            nbt.putString("craftingState", this.craftingState.name());

        if (this.field != null)
            nbt.put("field", this.field.write());

        if (this.currentRecipe != null)
            nbt.putString("recipe", this.currentRecipe.getId().toString());

        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);

        if (nbt.contains("craftingState"))
            this.craftingState = EnumCraftingState.valueOf(nbt.getString("craftingState"));

        if (nbt.contains("field"))
            this.field = FieldProjection.read(nbt.getCompound("field"));

        if (nbt.contains("recipe")) {
            this.recipeId = ResourceLocation.tryParse(nbt.getString("recipe"));
            loadRecipe();
        }
    }

    @Override
    public Optional<BlockPos> getMainProjectorPosition() {
        return Optional.ofNullable(worldPosition);
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
        syncAndMarkChanged();
    }

    public void setRecipe(MiniaturizationRecipe currentRecipe) {
        this.recipeId = currentRecipe == null ? null : currentRecipe.getId();
        this.currentRecipe = currentRecipe;
        syncAndMarkChanged();
    }

    private void syncAndMarkChanged() {
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        this.setChanged();
    }

    public EnumCraftingState getCraftingState() {
        return this.craftingState;
    }
}
