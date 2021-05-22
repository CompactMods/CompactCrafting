package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
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
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainFieldProjectorTile extends FieldProjectorTile implements ITickableTileEntity {

    private EnumCraftingState craftingState = EnumCraftingState.NOT_MATCHED;
    private FieldProjection field = null;
    private MiniaturizationRecipe currentRecipe = null;

    @Override
    public void setRemoved() {
        super.setRemoved();

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

        Optional<FieldProjection> field = FieldProjection.tryCreateFromPosition(level, this.worldPosition);
        if (field.isPresent()) {
            this.field = field.get();

            if (level != null && !level.isClientSide) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) level);
                data.ACTIVE_FIELDS.put(this.field.getCenterPosition(), ProjectorFieldData.fromInstance(this.field));
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
        if (this.field == null)
            return;

        if (this.level == null)
            return;

        AxisAlignedBB fieldBounds = field.getBounds();

        MiniaturizationFieldBlockData fieldBlocks = MiniaturizationFieldBlockData.getFromField(level, fieldBounds);

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
        Set<MiniaturizationRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE)
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
            boolean recipeMatches = recipe.matches(level, field.getFieldSize(), fieldBlocks);
            if (!recipeMatches)
                continue;

            matchedRecipe = recipe;
            updateCraftingState(EnumCraftingState.MATCHED);
            break;
        }

        this.currentRecipe = matchedRecipe;
    }

    public void setFieldInfo(FieldProjection field) {
        this.field = field;
        this.setChanged();
    }

    private void tickCrafting() {
        if (this.field != null) {
            AxisAlignedBB fieldBounds = field.getBounds();

            // Get out, client worlds
            if (level == null || level.isClientSide())
                return;

            // We grow the bounds check here a little to support patterns that are exactly the size of the field
            List<ItemEntity> catalystEntities = getCatalystsInField(fieldBounds.inflate(0.25), currentRecipe.getCatalyst().getItem());
            if (catalystEntities.size() > 0) {
                // We dropped a catalyst item in
                // At this point, we had a valid recipe and a valid catalyst entity
                // Start crafting
                switch (craftingState) {
                    case MATCHED:
                        updateCraftingState(EnumCraftingState.CRAFTING);

                        // We know the "recipe" in the field is an exact match already, so wipe the field
                        field.clearBlocks(level);

                        CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                        BlockPos centerField = field.getCenterPosition();
                        level.setBlockAndUpdate(centerField, Registration.FIELD_CRAFTING_PREVIEW_BLOCK.get().defaultBlockState());
                        FieldCraftingPreviewTile tile = (FieldCraftingPreviewTile) level.getBlockEntity(centerField);
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
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        if (this.field != null) {
            return field.getBounds().inflate(10);
        }

        return super.getRenderBoundingBox();
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
    }

    public EnumCraftingState getCraftingState() {
        return this.craftingState;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();

        if (this.field != null) {
            CompoundNBT fieldInfo = new CompoundNBT();
            fieldInfo.put("center", NBTUtil.writeBlockPos(this.field.getCenterPosition()));
            fieldInfo.putString("size", this.field.getFieldSize().name());
            tag.put("fieldInfo", fieldInfo);
        }

        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        if (tag.contains("fieldInfo")) {
            CompoundNBT fieldInfo = tag.getCompound("fieldInfo");
            BlockPos fCenter = NBTUtil.readBlockPos(fieldInfo.getCompound("center"));
            String sizeName = fieldInfo.getString("size");
            FieldProjectionSize size = FieldProjectionSize.valueOf(sizeName);

            this.field = FieldProjection.fromSizeAndCenter(size, fCenter);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        CompoundNBT nbt = super.save(compound);

        if (field != null) {
            CompoundNBT fieldInfo = new CompoundNBT();
            fieldInfo.put("center", NBTUtil.writeBlockPos(this.field.getCenterPosition()));
            nbt.put("fieldInfo", fieldInfo);
        }

        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);

        if(nbt.contains("fieldInfo")) {
            CompoundNBT fieldInfo = nbt.getCompound("fieldInfo");
            BlockPos center = NBTUtil.readBlockPos(fieldInfo.getCompound("center"));

            if(this.level != null && !this.level.isClientSide) {
                ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) level);
                ProjectorFieldData fieldData = data.ACTIVE_FIELDS.get(center);

                this.field = FieldProjection.fromSizeAndCenter(fieldData.size, fieldData.fieldCenter);
            }
        }
    }
}
