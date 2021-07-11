package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.proxies.block.FieldProxyBlock;
import com.robotgryphon.compactcrafting.proxies.data.BaseFieldProxyEntity;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.server.ServerConfig;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiniaturizationField implements IMiniaturizationField {

    private FieldProjectionSize size;
    private BlockPos center;
    private boolean loaded;

    private MiniaturizationRecipe currentRecipe = null;
    private EnumCraftingState craftingState;
    private long rescanTime;

    private World level;

    private final Set<BlockPos> proxies = new HashSet<>();

    public MiniaturizationField() {
    }

    private MiniaturizationField(FieldProjectionSize size, BlockPos center) {
        this.center = center;
        this.size = size;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
    }

    public static MiniaturizationField fromSizeAndCenter(FieldProjectionSize fieldSize, BlockPos center) {
        return new MiniaturizationField(fieldSize, center);
    }

    public FieldProjectionSize getFieldSize() {
        return this.size;
    }

    public BlockPos getCenter() {
        return center;
    }

    @Override
    public void setCenter(BlockPos center) {
        this.center = center;
    }

    @Override
    public void setSize(FieldProjectionSize size) {
        this.size = size;
    }

    @Override
    public void setLevel(World level) {
        this.level = level;
    }

    @Override
    public Stream<BlockPos> getProjectorPositions() {
        return this.size.getProjectorLocations(center);
    }

    public AxisAlignedBB getBounds() {
        return this.size.getBoundsAtPosition(center);
    }

    public Stream<BlockPos> getFilledBlocks() {
        return BlockPos.betweenClosedStream(getBounds().contract(1, 1, 1))
                .filter(p -> !level.isEmptyBlock(p))
                .map(BlockPos::immutable);
    }

    public AxisAlignedBB getFilledBounds() {
        BlockPos[] filled = getFilledBlocks().toArray(BlockPos[]::new);
        return BlockSpaceUtil.getBoundsForBlocks(filled);
    }

    public void clearBlocks() {
        // Remove blocks from the world
        getFilledBlocks()
                .sorted(Comparator.comparingInt(Vector3i::getY).reversed())
                .forEach(blockPos -> {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 7);

                    if (level instanceof ServerWorld) {
                        ((ServerWorld) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f,
                                1, 0d, 0.05D, 0D, 0.25d);
                    }
                });
    }

    public Optional<MiniaturizationRecipe> getCurrentRecipe() {
        return Optional.ofNullable(this.currentRecipe);
    }

    @Override
    public void clearRecipe() {
        this.currentRecipe = null;

        updateProxies();
    }

    private void updateProxies() {
        if (!proxies.isEmpty()) {
            proxies.forEach(pos -> {
                BlockState stateAtProxyPos = level.getBlockState(pos);
                if (stateAtProxyPos.getBlock() instanceof FieldProxyBlock && stateAtProxyPos.hasTileEntity()) {
                    BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(pos);
                    tile.recipeChanged(this, this.currentRecipe);
                }
            });
        }
    }

    @Override
    public void completeCraft() {
        this.currentRecipe = null;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
    }

    @Override
    public EnumCraftingState getCraftingState() {
        return craftingState;
    }

    @Override
    public void tick() {
        if(level == null)
            return;

        // Set in a block update handler to mark that the field has changed
        if(rescanTime > 0 && level.getGameTime() >= rescanTime) {
            doRecipeScan();
            this.rescanTime = 0;
            return;
        }

        if(getProjectorPositions().allMatch(level::isLoaded))
            tickCrafting();
    }

    public void tickCrafting() {
        AxisAlignedBB fieldBounds = getBounds();

        // Get out, client worlds
        if (level == null || level.isClientSide())
            return;

        if(this.currentRecipe == null)
            return;

        // We grow the bounds check here a little to support patterns that are exactly the size of the field
        List<ItemEntity> catalystEntities = getCatalystsInField(level, fieldBounds.inflate(0.25), currentRecipe.getCatalyst().getItem());
        if (catalystEntities.size() > 0) {
            // We dropped a catalyst item in
            // At this point, we had a valid recipe and a valid catalyst entity
            // Start crafting
            switch (craftingState) {
                case MATCHED:
                    this.craftingState = EnumCraftingState.CRAFTING;

                    // We know the "recipe" in the field is an exact match already, so wipe the field
                    clearBlocks();

                    CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                    BlockPos centerField = getCenter();
                    level.setBlockAndUpdate(centerField, Registration.FIELD_CRAFTING_PREVIEW_BLOCK.get().defaultBlockState());

                    // TODO - Expose this as a LazyOptional somehow
                    FieldCraftingPreviewTile tile = (FieldCraftingPreviewTile) level.getBlockEntity(centerField);
                    if (tile != null)
                        tile.setField(this);

                    break;

                case CRAFTING:
                    break;
            }
        }
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if (level == null)
            return;

        if(ServerConfig.FIELD_BLOCK_CHANGES.get())
            CompactCrafting.LOGGER.debug("Beginning field recipe scan: {}", this.center);

        Stream<BlockPos> filledBlocks = getFilledBlocks();

        // If no positions filled, exit early
        if (filledBlocks.count() == 0) {
            clearRecipe();
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
                .filter(recipe -> recipe.fitsInDimensions(getFilledBounds()))
                .collect(Collectors.toSet());

        /*
         * All the recipes we have registered won't fit in the filled bounds -
         * blocks were placed in a larger space than the max recipe size
         */
        CompactCrafting.LOGGER.trace("Matched a total of {} possible recipes.", recipes.size());
        if (recipes.isEmpty()) {
            clearRecipe();
            return;
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        MiniaturizationRecipe matchedRecipe = null;
        for (MiniaturizationRecipe recipe : recipes) {
            boolean recipeMatches = recipe.matches(level, this);
            if (!recipeMatches)
                continue;

            matchedRecipe = recipe;
            this.craftingState = EnumCraftingState.MATCHED;
            break;
        }

        this.currentRecipe = matchedRecipe;
        updateProxies();
    }

    @Override
    public void setCraftingState(EnumCraftingState state) {
        this.craftingState = state;
    }

    private List<ItemEntity> getCatalystsInField(IWorld level, AxisAlignedBB fieldBounds, Item itemFilter) {
        List<ItemEntity> itemsInRange = level.getEntitiesOfClass(ItemEntity.class, fieldBounds);
        return itemsInRange.stream()
                .filter(ise -> ise.getItem().getItem() == itemFilter)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public void checkLoaded() {
        CompactCrafting.LOGGER.trace("Checking loaded state.");
        this.loaded = level.isAreaLoaded(center, size.getProjectorDistance() + 3);

        if(loaded) {
            getProjectorPositions().forEach(proj -> {
                FieldProjectorBlock.activateProjector(level, proj, this.size);
            });
        }
    }

    @Override
    public void markFieldChanged() {
        // clear the recipe immediately so people can't dupe items or juke the projectors
        this.clearRecipe();

        // set a distant rescan duration to make the field revalidate itself after a second or two
        this.rescanTime = level.getGameTime() + 30;
    }

    @Override
    public Set<BlockPos> getProxies() {
        return proxies;
    }

    @Override
    public void registerProxyAt(BlockPos position) {
        proxies.add(position);
    }

    @Override
    public void unregisterProxyAt(BlockPos position) {
        proxies.remove(position);
    }
}
