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
import dev.compactmods.crafting.projector.FieldProjectorBlock;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

public class MiniaturizationField implements IMiniaturizationField {

    private MiniaturizationFieldSize size;
    private BlockPos center;
    private boolean loaded;

    @Nullable
    private MiniaturizationRecipe currentRecipe = null;

    @Nullable
    private ResourceLocation recipeId = null;

    private EnumCraftingState craftingState;
    private long rescanTime;

    private World level;
    private int craftingProgress = 0;

    private final HashSet<LazyOptional<IFieldListener>> listeners = new HashSet<>();

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

        /*
         * Dry run - we have the data from the field on what's filled and how large
         * the area is. Run through the recipe list and filter based on that, so
         * we remove all the recipes that are definitely larger than the currently
         * filled space.
         */
        Set<MiniaturizationRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE)
                .stream().map(r -> (MiniaturizationRecipe) r)
                .filter(recipe -> BlockSpaceUtil.boundsFitsInside(recipe.getDimensions(), getFilledBounds()))
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
            RecipeBlocks blocks = RecipeBlocks.create(level, recipe.getComponents(), getFilledBounds());
            boolean recipeMatches = recipe.matches(blocks);
            if (!recipeMatches)
                continue;

            matchedRecipe = recipe;
            break;
        }

        setRecipe(matchedRecipe);
    }

    private void setRecipe(MiniaturizationRecipe recipe) {
        this.currentRecipe = recipe;
        this.recipeId = recipe != null ? recipe.getRecipeIdentifier() : null;
        this.craftingProgress = 0;

        setCraftingState(recipe != null ? EnumCraftingState.MATCHED : EnumCraftingState.NOT_MATCHED);

        // Send tracking client updates
        if (!level.isClientSide) {
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(center)),
                    new FieldRecipeChangedPacket(this)
            );
        }

        // Update all listeners as well
        listeners.forEach(l -> l.ifPresent(fl -> {
            fl.onRecipeChanged(this, recipe);

            if (craftingState == EnumCraftingState.MATCHED)
                fl.onRecipeMatched(this, recipe);
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
            getProjectorPositions().forEach(proj -> FieldProjectorBlock.activateProjector(level, proj, this.size));

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
}
