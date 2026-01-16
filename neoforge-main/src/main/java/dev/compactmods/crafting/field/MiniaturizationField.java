package dev.compactmods.crafting.field;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.ITickingMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.projector.FieldProjectorSet;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.crafting.CraftingHelper;
import dev.compactmods.crafting.events.WorldEventHandler;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.network.FieldRecipeChangedPacket;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import io.reactivex.rxjava3.disposables.Disposable;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiniaturizationField implements IMiniaturizationField<MiniaturizationRecipe>,
        IMutableMiniaturizationField, ITickingMiniaturizationField {

    private final Level level;
    private final MiniaturizationFieldSize size;
    private final BlockPos center;
    private final FieldProjectorSet projectors;

    private boolean areaLoaded;

    @Deprecated(forRemoval = true)
    private boolean disabled = false;

    // TODO - Create MatchedMiniaturizationRecipe
    private RecipeHolder<MiniaturizationRecipe> currentRecipe = null;
    private StructureTemplate matchedBlocks;
    private Set<Item> matchedCatalysts;
    private int craftingProgress = 0;

    // Crafting State
    private EnumCraftingState craftingState;
    private long rescanTime;

    private static Disposable CHUNK_LISTENER;

    // Metallic Orange
    public static final DustParticleOptions RECIPE_MATCHED_PARTICLE_OPTS = new DustParticleOptions(ARGB.color(210, 226, 99, 16), 1);

    // Eucalyptus (Turquoise Green)
    public static final DustParticleOptions RECIPE_FINISHED_PARTICLE_OPTS = new DustParticleOptions(ARGB.color(210, 68, 215, 168), 1);

    public MiniaturizationField(Level level, MiniaturizationFieldSize size, BlockPos center) {
        this.level = level;
        this.center = center;
        this.size = size;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
        this.projectors = new FieldProjectorSet(new WeakReference<>(level), this.size.getProjectorLocations(this.center).collect(Collectors.toSet()), this.size);

        setupChunkListener();
    }

//    public MiniaturizationField(CompoundTag nbt) {
//        this.craftingState = EnumCraftingState.valueOf(nbt.getString("state"));
//
//        this.center = NbtUtils.readBlockPos(nbt, "center").orElseThrow();
//        this.size = MiniaturizationFieldSize.valueOf(nbt.getString("size"));
//
//        setupChunkListener();
//
//        // temp load recipe
//        if (nbt.contains("recipe")) {
//            this.recipeId = Identifier.parse(nbt.getString("recipe"));
//            this.craftingProgress = nbt.getInt("progress");
//        }
//
//        if (nbt.contains("matchedBlocks")) {
//            StructureTemplate t = new StructureTemplate();
//            t.load(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("matchedBlocks"));
//            this.matchedBlocks = t;
//        } else {
//            this.matchedBlocks = null;
//        }
//
//        this.disabled = nbt.contains("disabled") && nbt.getBoolean("disabled");
//    }

    private void setupChunkListener() {
        // add projector and central chunks
        final Set<ChunkPos> insideChunks = this.projectors
                .locations()
                .stream()
                .map(ChunkPos::containing)
                .collect(Collectors.toSet());

        insideChunks.add(ChunkPos.containing(center));

        CHUNK_LISTENER = WorldEventHandler.CHUNK_CHANGES.filter(ce -> {
            boolean sameLevel = ((LevelChunk) ce.getChunk()).getLevel().dimension().equals(level.dimension());
            boolean watchedChunk = insideChunks.contains(ce.getChunk().getPos());
            return sameLevel && watchedChunk;
        }).subscribe((changed) -> this.checkLoaded());
    }

    @Override
    public void dispose() {
        if (!CHUNK_LISTENER.isDisposed())
            CHUNK_LISTENER.dispose();
    }

    public MiniaturizationFieldSize getFieldSize() {
        return this.size;
    }

    public BlockPos getCenter() {
        return center;
    }

    @Override
    public int getProgress() {
        if (craftingState != EnumCraftingState.CRAFTING)
            return 0;

        return craftingProgress;
    }

//    private void getRecipeFromId() {
//        // Load recipe information from temporary id variable
//        if (level == null || this.recipeId == null) {
//            clearRecipe();
//        } else {
//            final var r = level.getRecipeManager().byKey(recipeId);
//            r.ifPresentOrElse(recipe -> {
//                this.currentRecipe = (MiniaturizationRecipe) recipe.value();
//                if (craftingState == EnumCraftingState.NOT_MATCHED)
//                    setCraftingState(EnumCraftingState.MATCHED);
//

    /// /                this.listeners.forEach(li -> li.ifPresent(l -> {
    /// /                    l.onRecipeChanged(this, this.currentRecipe);
    /// /                    l.onRecipeMatched(this, this.currentRecipe);
    /// /                }));
//
//            }, this::clearRecipe);
//        }
//    }
    @Override
    public FieldProjectorSet getProjectors() {
        return this.projectors;
    }

    public AABB getBounds() {
        return this.size.getBoundsAtPosition(center);
    }

    public Stream<BlockPos> getFilledBlocks() {
        return BlockSpaceUtil.getBlocksIn(getBounds())
                .filter(p -> !level.isEmptyBlock(p))
                .map(BlockPos::immutable);
    }

    public AABB getFilledBounds() {
        BlockPos[] filled = getFilledBlocks().toArray(BlockPos[]::new);
        return BlockSpaceUtil.getBoundsForBlocks(filled);
    }

    public void clearBlocks() {
        // Remove blocks from the world
        getFilledBlocks()
                .sorted(Comparator.comparingInt(Vec3i::getY).reversed()) // top down so stuff like redstone doesn't drop as items
                .forEach(blockPos -> {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 7);

                    if (level instanceof ServerLevel) {
                        ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f,
                                1, 0d, 0.05D, 0D, 0.25d);
                    }
                });
    }

    @Override
    public void setRecipe(RecipeHolder<MiniaturizationRecipe> recipe) {
        this.currentRecipe = recipe;
        this.craftingProgress = 0;

        if (craftingState == EnumCraftingState.NOT_MATCHED)
            setCraftingState(EnumCraftingState.MATCHED);

//        this.listeners.forEach(li -> li.ifPresent(l -> {
//            l.onRecipeChanged(this, this.currentRecipe);
//            l.onRecipeMatched(this, this.currentRecipe);
//        }));
    }

    @Override
    public void clearRecipe() {
        this.currentRecipe = null;
        this.craftingProgress = 0;
        setCraftingState(EnumCraftingState.NOT_MATCHED);

//        listeners.forEach(l -> l.ifPresent(listener -> {
//            listener.onRecipeChanged(this, this.currentRecipe);
//            listener.onRecipeCleared(this);
//        }));
    }

    @Override
    public EnumCraftingState getCraftingState() {
        return craftingState;
    }

    @Override
    public void tick() {
        if (this.disabled || !areaLoaded)
            return;

        switch (this.craftingState) {
            case NOT_MATCHED:
                // Set in a block update handler to mark that the field has changed
                if (rescanTime > 0 && level.getGameTime() >= rescanTime) {
                    doRecipeScan();
                    this.rescanTime = 0;
                    break;
                }
                break;

            case MATCHED:
                AABB fieldBounds = getBounds();
                searchAndConsumeCatalysts(fieldBounds);
                break;

            case CRAFTING:
                tickCrafting();
                break;
        }
    }

    private void searchAndConsumeCatalysts(AABB fieldBounds) {
        // We grow the bounds check here a little to support patterns that are exactly the size of the field
        List<ItemEntity> catalystEntities = getCatalystsInField(level, fieldBounds.inflate(0.25), currentRecipe.value().catalystTest());
        if (!catalystEntities.isEmpty()) {

            matchedCatalysts = catalystEntities.stream()
                    .map((ItemEntity t) -> t.getItem().getItem())
                    .collect(Collectors.toSet());

            var foundPosition = catalystEntities.getFirst().position();

            // Only remove items and clear the field on servers
            if (!level.isClientSide()) {
                CraftingHelper.consumeCatalystItem(catalystEntities.get(0), 1);

                // We know the "recipe" in the field is an exact match already, so wipe the field
                clearBlocks();
            } else {
                for (int i = 0; i < 5; i++) {
                    RandomSource random = level.getRandom();
                    level.addParticle(ParticleTypes.LARGE_SMOKE,
                            foundPosition.x + random.nextDouble(),
                            foundPosition.y + random.nextDouble(),
                            foundPosition.z + random.nextDouble(),
                            0.0, 0.0, 0.0);
                }
            }

            setCraftingState(EnumCraftingState.CRAFTING);
        }
    }

    private void tickCrafting() {
        if (this.currentRecipe == null)
            return;

        craftingProgress++;
        if (craftingProgress >= currentRecipe.value().getCraftingTime()) {
            for (ItemStack is : currentRecipe.value().getOutputs()) {
                ItemEntity itemEntity = new ItemEntity(level, center.getX() + 0.5f, center.getY() + 0.5f, center.getZ() + 0.5f, is);
                level.addFreshEntity(itemEntity);
            }

            spawnParticlesAtProjectors(RECIPE_FINISHED_PARTICLE_OPTS);

            IMiniaturizationRecipe completed = this.currentRecipe.value();
            clearRecipe();

//                    listeners.forEach(l -> l.ifPresent(listener -> listener.onRecipeCompleted(this, completed)));
        }
    }

    public void spawnParticlesAtProjectors(ParticleOptions opts) {
        projectors.locations().forEach(proj -> {
            var center = Vec3.atCenterOf(proj);
            for (int i = 0; i < 10; i++) {
                RandomSource random = level.getRandom();
                level.addParticle(opts,
                        center.x + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        center.y + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        center.z + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        0.0, 0.0, 0.0);
            }
        });
    }

    /**
     * Scans the field and attempts to match a recipe that's placed in it.
     */
    public void doRecipeScan() {
        if (level == null)
            return;

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

        AABB filledBounds = getFilledBounds();

        /*
         * Dry run - we have the data from the field on what's filled and how large
         * the area is. Run through the recipe list and filter based on that, so
         * we remove all the recipes that are definitely larger than the currently
         * filled space.
         */
        var recipes = level.getServer().getRecipeManager()
                .recipeMap()
                .byType(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE.get())
                .stream()
                .filter(recipe -> BlockSpaceUtil.boundsFitsInside(recipe.value().getDimensions(), filledBounds))
                .collect(Collectors.toSet());

        /*
         * All the recipes we have registered won't fit in the filled bounds -
         * blocks were placed in a larger space than the max recipe size
         */
        CompactCrafting.LOGGER.debug("Matched a total of {} possible recipes.", recipes.size());
        if (recipes.isEmpty()) {
            clearRecipe();
            return;
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        this.currentRecipe = null;
        this.craftingProgress = 0;

        for (var recipe : recipes) {

            RecipeBlocks blocks = RecipeBlocks.create(level, recipe.value().getComponents(), filledBounds);
            boolean recipeMatches = recipe.value().matches(blocks);
            if (!recipeMatches)
                continue;

            this.matchedBlocks = new StructureTemplate();

            final AABB fieldBounds = size.getBoundsAtPosition(center);
            BlockPos minPos = BlockPos.containing(fieldBounds.minX, fieldBounds.minY, fieldBounds.minZ);

            // boolean here is to capture entities - TODO maybe
            matchedBlocks.fillFromWorld(level, minPos, size.getBoundsAsBlockPos(), false, null);

            this.currentRecipe = recipe;
            break;
        }

        setCraftingState(currentRecipe != null ? EnumCraftingState.MATCHED : EnumCraftingState.NOT_MATCHED);
        if (currentRecipe != null)
            spawnParticlesAtProjectors(RECIPE_MATCHED_PARTICLE_OPTS);

        // Send tracking client updates
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            PacketDistributor.sendToPlayersTrackingChunk(sl, ChunkPos.containing(center),
                    new FieldRecipeChangedPacket(this.center, Optional.ofNullable(this.currentRecipe)));
        }

        // Update all listeners as well
        final var finalMatchedRecipe = this.currentRecipe;
//        listeners.forEach(l -> l.ifPresent(fl -> {
//            fl.onRecipeChanged(this, finalMatchedRecipe);
//
//            if (craftingState == EnumCraftingState.MATCHED)
//                fl.onRecipeMatched(this, finalMatchedRecipe);
//        }));
    }

    @Override
    public void setCraftingState(EnumCraftingState state) {
        this.craftingState = state;
    }

    private List<ItemEntity> getCatalystsInField(LevelAccessor level, AABB fieldBounds, ItemPredicate itemFilter) {
        List<ItemEntity> itemsInRange = level.getEntitiesOfClass(ItemEntity.class, fieldBounds);
        return itemsInRange.stream()
                .filter(ise -> itemFilter.test(ise.getItem()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAreaLoaded() {
        return areaLoaded || level.isClientSide();
    }

    public void checkLoaded() {
        this.areaLoaded = level.isAreaLoaded(center, size.getProjectorDistance() + 3);

        if (areaLoaded) {
//            listeners.forEach(l -> l.ifPresent(fl -> fl.onFieldActivated(this)));
        }
    }

    @Override
    public void fieldContentsChanged() {
        // clear the recipe immediately so people can't dupe items or break the projectors
        this.clearRecipe();

        // set a distant rescan duration to make the field revalidate itself after a second or two
        this.rescanTime = level.getGameTime() + 30;
    }

//    @Override
//    public void registerListener(LazyOptional<IFieldListener> listener) {
//        this.listeners.add(listener);
//        listener.addListener(fl -> {
//            CompactCrafting.LOGGER.debug("Removing listener: {}", fl);
//            this.listeners.remove(fl);
//        });
//    }

//    @Override
//    public CompoundTag serverData() {
//        CompoundTag nbt = new CompoundTag();
//        nbt.putString("size", size.name());
//        nbt.put("center", NbtUtils.writeBlockPos(center));
//
//        nbt.putString("state", craftingState.name());
//
//        if (currentRecipe != null) {
//            nbt.putString("recipe", currentRecipe.getRecipeIdentifier().toString());
//            nbt.putInt("progress", craftingProgress);
//        }
//
//        if (matchedBlocks != null) {
//            nbt.put("matchedBlocks", matchedBlocks.save(new CompoundTag()));
//        }
//
//        nbt.putBoolean("disabled", this.disabled);
//
//        return nbt;
//    }

    @Override
    public void handleDestabilize() {
        if (craftingState != EnumCraftingState.CRAFTING || matchedBlocks == null)
            return;

        if (level.isClientSide()) return;

        // TODO - Look at dumping items into an inventory if it's attached to a projector, helps automation (in the weird cases)
        boolean restoreBlocks = false;
        boolean restoreCatalyst = false;
        switch (ServerConfig.DESTABILIZE_HANDLING) {
            case RESTORE_ALL:
                restoreBlocks = true;
                restoreCatalyst = true;
                break;

            case RESTORE_BLOCKS:
            case DESTROY_CATALYST:
                restoreBlocks = true;
                break;

            case RESTORE_CATALYST:
            case DESTROY_BLOCKS:
                restoreCatalyst = true;
                break;

            case DESTROY_ALL:
                break;
        }

        if (restoreBlocks) {
            AABB bounds = getBounds();
            BlockPos placeAt = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
            // TODO - Check the const here, 2 may be wrong
            matchedBlocks.placeInWorld((ServerLevelAccessor) level, placeAt, placeAt,
                    new StructurePlaceSettings(), level.getRandom(), 2);
        }

        if (currentRecipe != null) {
            final ItemPredicate catalyst = currentRecipe.value().catalystTest();
            if (restoreCatalyst) {
                final BlockPos northLoc = size.getProjectorLocationForDirection(center, Direction.NORTH);

                for (Item cat : matchedCatalysts) {
                    final ItemEntity ie = new ItemEntity(level,
                            northLoc.getX(), center.getY() + 1.5f, northLoc.getZ(),
                            new ItemStack(cat));

                    // ie.setNoGravity(true);

                    level.addFreshEntity(ie);
                }
            }
        }
    }

    @Override
    public void disable() {
        this.disabled = true;
        if (this.craftingState != EnumCraftingState.NOT_MATCHED)
            handleDestabilize();

        getProjectors().disableAll();

        if (this.level instanceof ServerLevel sl) {
            FieldDeactivatedPacket update = new FieldDeactivatedPacket(size, center, getProjectors().locations().stream().toList());
            PacketDistributor.sendToPlayersTrackingChunk(sl, ChunkPos.containing(center), update);
        }
    }

    @Override
    public void enable() {
        this.disabled = false;
        fieldContentsChanged();
        this.projectors.enableAll();

        if (this.level instanceof ServerLevel sl) {
            // FIXME
            // FieldDeactivatedPacket update = new FieldActivatedPacket(this, this.clientData());
            // PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(center), update);
        }
    }

    @Override
    public void checkRedstone() {
        this.disabled = getProjectors()
                .locations()
                .stream()
                .anyMatch(proj -> level.getBestNeighborSignal(proj) > 0);

        if (disabled) disable();
        else enable();
    }

    @Override
    public boolean enabled() {
        return !this.disabled;
    }

    @Override
    public RecipeHolder<MiniaturizationRecipe> recipeHolder() {
        return currentRecipe;
    }

    @Override
    public MiniaturizationRecipe currentRecipe() {
        return currentRecipe.value();
    }

    // FIXME
//    @Override
//    public Tag serializeNBT() {
//        CompoundTag fieldInfo = new CompoundTag();
//        fieldInfo.put("center", NbtUtils.writeBlockPos(center));
//        fieldInfo.putString("size", size.name());
//
//        fieldInfo.putString("craftingState", craftingState.name());
//
//        if (currentRecipe != null)
//            fieldInfo.putString("recipe", currentRecipe.getRecipeIdentifier().toString());
//
//        return fieldInfo;
//    }
//
//    @Override
//    public void deserializeNBT(Tag nbt) {
//        if (nbt instanceof CompoundTag fieldInfo) {
//            this.center = NbtUtils.readBlockPos(fieldInfo.getCompound("center"));
//            this.size = MiniaturizationFieldSize.valueOf(fieldInfo.getString("size"));
//
//            if (fieldInfo.contains("craftingState")) {
//                this.craftingState = EnumCraftingState.valueOf(fieldInfo.getString("craftingState"));
//            }
//        }
//    }
}