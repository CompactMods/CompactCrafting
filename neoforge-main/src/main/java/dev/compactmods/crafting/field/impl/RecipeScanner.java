package dev.compactmods.crafting.field.impl;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipes;
import dev.compactmods.crafting.recipes.RecipeScanResult;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeScanner {
    /**
     * Scans the projectors and attempts to match a recipe that's placed in it.
     */
    public static Optional<RecipeScanResult> doRecipeScan(MiniaturizationFieldAccess fieldAccess) {
        if (fieldAccess.level() == null)
            return Optional.empty();

        CompactCrafting.LOGGER.debug("Beginning projectors recipe scan: {}",
                fieldAccess.location().center());

        Stream<BlockPos> filledBlocks = fieldAccess.getFilledBlocks();

        // If no positions filled, exit early
        if (filledBlocks.findAny().isEmpty())
            return Optional.empty();

        // ===========================================================================================================
        //   RECIPE BEGIN
        // ===========================================================================================================

        AABB filledBounds = fieldAccess.getFilledBounds();

        /*
         * Dry run - we have the data from the projectors on what's filled and how large
         * the area is. Run through the recipe list and filter based on that, so
         * we remove all the recipes that are definitely larger than the currently
         * filled space.
         */
        var recipes = fieldAccess.level().getServer()
                .getRecipeManager()
                .recipeMap()
                .byType(MiniaturizationRecipes.MINIATURIZATION_RECIPE.get())
                .stream()
                .filter(recipe -> BlockSpaceUtil.boundsFitsInside(recipe.value().getDimensions(), fieldAccess.getBounds()))
                .collect(Collectors.toSet());

        /*
         * All the recipes we have registered won't fit in the filled bounds -
         * blocks were placed in a larger space than the max recipe size
         */
        CompactCrafting.LOGGER.debug("Matched a total of {} possible recipes.", recipes.size());
        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        // Begin recipe dry run - loop, check bottom layer for matches
        for (var recipe : recipes) {

            RecipeBlocks blocks = RecipeBlocks.create(fieldAccess.level(), recipe.value().getComponents(), filledBounds);
            boolean recipeMatches = recipe.value().matches(blocks);
            if (!recipeMatches)
                continue;

            final var matchedBlocks = new StructureTemplate();

            BlockPos minPos = BlockPos.containing(fieldAccess.getBounds().getMinPosition());

            matchedBlocks.fillFromWorld(fieldAccess.level(), minPos, fieldAccess.location().boundsVec3i(),
                    false,
                    List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR));

            final var result = new RecipeScanResult(matchedBlocks, recipe);
            return Optional.of(result);
        }

        return Optional.empty();

        // Send tracking client updates
//        if (!level.isClientSide() && level instanceof ServerLevel sl) {
//            BlockPos centerBlock = MathUtil.toBlockPosition(location.center());
//            PacketDistributor.sendToPlayersTrackingChunk(sl, ChunkPos.containing(centerBlock),
//                    new FieldRecipeChangedPacket(location, Optional.ofNullable(this.currentRecipe)));
//        }
    }

    public static List<ItemEntity> getCatalystsInField(MiniaturizationFieldAccess fieldAccess, MiniaturizationRecipe recipe) {
        final var searchArea = fieldAccess.getBounds().inflate(0.25);
        List<ItemEntity> itemsInRange = fieldAccess.level().getEntitiesOfClass(ItemEntity.class, searchArea);

        return itemsInRange.stream()
                .filter(ise -> recipe.catalystMatcher().test(ise.getItem()))
                .collect(Collectors.toList());
    }
}
