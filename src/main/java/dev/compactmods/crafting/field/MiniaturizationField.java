package dev.compactmods.crafting.field;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.crafting.CraftingHelper;
import dev.compactmods.crafting.network.FieldRecipeChangedPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

public class MiniaturizationField implements IMiniaturizationField {

    private MiniaturizationFieldSize size;
    private BlockPos center;
    private boolean loaded;

    @Nullable
    private MiniaturizationRecipe currentRecipe = null;
    private Template matchedBlocks;

    @Nullable
    private ResourceLocation recipeId = null;

    private EnumCraftingState craftingState;
    private long rescanTime;

    private World level;
    private int craftingProgress = 0;

    private final HashSet<LazyOptional<IFieldListener>> listeners = new HashSet<>();
    private LazyOptional<IMiniaturizationField> lazyReference = LazyOptional.empty();

    public MiniaturizationField() {
    }

    private MiniaturizationField(MiniaturizationFieldSize size, BlockPos center) {
        this.center = center;
        this.size = size;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
    }

    public static MiniaturizationField fromSizeAndCenter(MiniaturizationFieldSize fieldSize, BlockPos center) {
        return new MiniaturizationField(fieldSize, center);
    }

    public MiniaturizationFieldSize getFieldSize() {
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
    public void setSize(MiniaturizationFieldSize size) {
        this.size = size;
    }

    @Override
    public int getProgress() {
        if (craftingState != EnumCraftingState.CRAFTING)
            return 0;

        return craftingProgress;
    }

    @Override
    public void setLevel(World level) {
        this.level = level;

        getRecipeFromId();
    }

    private void getRecipeFromId() {
        // Load recipe information from temporary id variable
        if (level != null && this.recipeId != null) {
            final Optional<? extends IRecipe<?>> r = level.getRecipeManager().byKey(recipeId);
            if (!r.isPresent()) {
                clearRecipe();
                return;
            }

            r.ifPresent(rec -> {
                this.currentRecipe = (MiniaturizationRecipe) rec;
                if (craftingState == EnumCraftingState.NOT_MATCHED)
                    setCraftingState(EnumCraftingState.MATCHED);

                this.listeners.forEach(li -> li.ifPresent(l -> {
                    l.onRecipeChanged(this, this.currentRecipe);
                    l.onRecipeMatched(this, this.currentRecipe);
                }));
            });
        } else {
            clearRecipe();
        }
    }

    @Override
    public Stream<BlockPos> getProjectorPositions() {
        return this.size.getProjectorLocations(center);
    }

    public AxisAlignedBB getBounds() {
        return this.size.getBoundsAtPosition(center);
    }

    public Stream<BlockPos> getFilledBlocks() {
        return BlockSpaceUtil.getBlocksIn(getBounds())
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
                .sorted(Comparator.comparingInt(Vector3i::getY).reversed()) // top down so stuff like redstone doesn't drop as items
                .forEach(blockPos -> {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 7);

                    if (level instanceof ServerWorld) {
                        ((ServerWorld) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f,
                                1, 0d, 0.05D, 0D, 0.25d);
                    }
                });
    }

    public Optional<IMiniaturizationRecipe> getCurrentRecipe() {
        return Optional.ofNullable(this.currentRecipe);
    }

    @Override
    public void clearRecipe() {
        this.recipeId = null;
        this.currentRecipe = null;
        this.craftingProgress = 0;
        setCraftingState(EnumCraftingState.NOT_MATCHED);

        listeners.forEach(l -> l.ifPresent(listener -> {
            listener.onRecipeChanged(this, this.currentRecipe);
            listener.onRecipeCleared(this);
        }));
    }

    @Override
    public EnumCraftingState getCraftingState() {
        return craftingState;
    }

    @Override
    public void tick() {
        if (level == null)
            return;

        // Set in a block update handler to mark that the field has changed
        if (rescanTime > 0 && level.getGameTime() >= rescanTime) {
            doRecipeScan();
            this.rescanTime = 0;
            return;
        }

        if (getProjectorPositions().allMatch(level::isLoaded))
            tickCrafting();
    }

    public void tickCrafting() {
        AxisAlignedBB fieldBounds = getBounds();

        if (level == null || this.currentRecipe == null)
            return;


        switch (craftingState) {
            case MATCHED:

                // We grow the bounds check here a little to support patterns that are exactly the size of the field
                List<ItemEntity> catalystEntities = getCatalystsInField(level, fieldBounds.inflate(0.25), currentRecipe.getCatalyst().getItem());
                if (catalystEntities.size() > 0) {

                    // Only remove items and clear the field on servers
                    if (!level.isClientSide) {
                        CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                        // We know the "recipe" in the field is an exact match already, so wipe the field
                        clearBlocks();
                    }

                    setCraftingState(EnumCraftingState.CRAFTING);
                }

                break;

            case CRAFTING:
                craftingProgress++;
                if (craftingProgress >= currentRecipe.getCraftingTime()) {
                    for (ItemStack is : currentRecipe.getOutputs()) {
                        ItemEntity itemEntity = new ItemEntity(level, center.getX() + 0.5f, center.getY() + 0.5f, center.getZ() + 0.5f, is);
                        level.addFreshEntity(itemEntity);
                    }

                    IMiniaturizationRecipe completed = this.currentRecipe;

                    clearRecipe();

                    listeners.forEach(l -> l.ifPresent(listener -> listener.onRecipeCompleted(this, completed)));
                }

                break;
        }
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if (level == null)
            return;

        // TODO - This is giving bad data with mixed layers (CM4 recipes) - fix it

        if (ServerConfig.FIELD_BLOCK_CHANGES.get())
            CompactCrafting.LOGGER.debug("Beginning field recipe scan: {}", this.center);

        Stream<BlockPos> filledBlocks = getFilledBlocks();

        // If no positions filled, exit early
        if (!filledBlocks.findAny().isPresent()) {
            clearRecipe();
            return;
        }

        // ===========================================================================================================
        //   RECIPE BEGIN
        // ===========================================================================================================

        AxisAlignedBB filledBounds = getFilledBounds();

        /*
         * Dry run - we have the data from the field on what's filled and how large
         * the area is. Run through the recipe list and filter based on that, so
         * we remove all the recipes that are definitely larger than the currently
         * filled space.
         */
        Set<MiniaturizationRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE)
                .stream().map(r -> (MiniaturizationRecipe) r)
                .filter(recipe -> BlockSpaceUtil.boundsFitsInside(recipe.getDimensions(), filledBounds))
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
        this.currentRecipe = null;
        this.recipeId = null;
        this.craftingProgress = 0;


        for (MiniaturizationRecipe recipe : recipes) {

            RecipeBlocks blocks = RecipeBlocks.create(level, recipe.getComponents(), filledBounds);
            boolean recipeMatches = recipe.matches(blocks);
            if (!recipeMatches)
                continue;

            this.matchedBlocks = new Template();

            final AxisAlignedBB fieldBounds = size.getBoundsAtPosition(center);
            BlockPos minPos = new BlockPos(fieldBounds.minX, fieldBounds.minY, fieldBounds.minZ);

            // boolean here is to capture entities - TODO maybe
            matchedBlocks.fillFromWorld(level, minPos, size.getBoundsAsBlockPos(), false, null);

            this.currentRecipe = recipe;
            this.recipeId = currentRecipe.getRecipeIdentifier();
            break;
        }

        setCraftingState(currentRecipe != null ? EnumCraftingState.MATCHED : EnumCraftingState.NOT_MATCHED);

        // Send tracking client updates
        if (!level.isClientSide) {
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(center)),
                    new FieldRecipeChangedPacket(this)
            );
        }

        // Update all listeners as well
        final MiniaturizationRecipe finalMatchedRecipe = this.currentRecipe;
        listeners.forEach(l -> l.ifPresent(fl -> {
            fl.onRecipeChanged(this, finalMatchedRecipe);

            if (craftingState == EnumCraftingState.MATCHED)
                fl.onRecipeMatched(this, finalMatchedRecipe);
        }));
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
        return loaded || level.isClientSide;
    }

    public void checkLoaded() {
        CompactCrafting.LOGGER.trace("Checking loaded state.");
        this.loaded = level.isAreaLoaded(center, size.getProjectorDistance() + 3);

        if (loaded) {
            listeners.forEach(l -> l.ifPresent(fl -> fl.onFieldActivated(this)));
        }
    }

    @Override
    public void fieldContentsChanged() {
        // clear the recipe immediately so people can't dupe items or break the projectors
        this.clearRecipe();

        // set a distant rescan duration to make the field revalidate itself after a second or two
        this.rescanTime = level.getGameTime() + 30;
    }

    @Override
    public void registerListener(LazyOptional<IFieldListener> listener) {
        this.listeners.add(listener);
        listener.addListener(fl -> {
            CompactCrafting.LOGGER.debug("Removing listener: {}", fl);
            this.listeners.remove(fl);
        });
    }

    // TODO - Basic data class codec for necessary info ?

    @Override
    public CompoundNBT serverData() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("size", size.name());
        nbt.put("center", NBTUtil.writeBlockPos(center));

        nbt.putString("state", craftingState.name());

        if (currentRecipe != null) {
            nbt.putString("recipe", currentRecipe.getRecipeIdentifier().toString());
            nbt.putInt("progress", craftingProgress);
        }

        if (matchedBlocks != null) {
            nbt.put("matchedBlocks", matchedBlocks.save(new CompoundNBT()));
        }

        return nbt;
    }

    @Override
    public void loadServerData(CompoundNBT nbt) {
        this.craftingState = EnumCraftingState.valueOf(nbt.getString("state"));

        // temp load recipe
        if (nbt.contains("recipe")) {
            this.recipeId = new ResourceLocation(nbt.getString("recipe"));
            this.craftingProgress = nbt.getInt("progress");
        }

        if (nbt.contains("matchedBlocks")) {
            Template t = new Template();
            t.load(nbt.getCompound("matchedBlocks"));
            this.matchedBlocks = t;
        } else {
            this.matchedBlocks = null;
        }
    }

    @Override
    public void setProgress(int progress) {
        this.craftingProgress = progress;
    }

    @Override
    public void setRecipe(ResourceLocation id) {
        this.recipeId = id;
        getRecipeFromId();
    }

    @Override
    public void handleProjectorBroken() {
        if (craftingState != EnumCraftingState.CRAFTING || matchedBlocks == null)
            return;

        if (level.isClientSide) return;

        boolean restoreBlocks = false;
        boolean restoreCatalyst = false;
        switch (ServerConfig.DESTABILIZE_HANDLING) {
            case RESTORE_ALL:
                restoreBlocks = true;
                restoreCatalyst = true;
                break;

            case RESTORE_BLOCKS:
                restoreBlocks = true;
                break;

            case RESTORE_CATALYST:
                restoreCatalyst = true;
                break;

            case DESTROY_ALL:
                break;

            case DESTROY_BLOCKS:
                restoreCatalyst = true;
                break;

            case DESTROY_CATALYST:
                restoreBlocks = true;
                break;
        }

        if (restoreBlocks) {
            AxisAlignedBB bounds = getBounds();
            matchedBlocks.placeInWorld((IServerWorld) level, new BlockPos(bounds.minX, bounds.minY, bounds.minZ),
                    new PlacementSettings(), level.random);
        }

        final ItemStack catalyst = currentRecipe.getCatalyst();
        if (restoreCatalyst && !catalyst.isEmpty()) {
            final BlockPos northLoc = size.getProjectorLocationForDirection(center, Direction.NORTH);
            final ItemEntity ie = new ItemEntity(level, northLoc.getX(), center.getY() + 1.5f, northLoc.getZ(), catalyst);
            // ie.setNoGravity(true);

            level.addFreshEntity(ie);
        }
    }

    @Override
    public LazyOptional<IMiniaturizationField> getRef() {
        return lazyReference;
    }

    public void setRef(LazyOptional<IMiniaturizationField> lazyReference) {
        this.lazyReference = lazyReference;
    }
}
