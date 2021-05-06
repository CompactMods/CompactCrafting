package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeHelper {
    private RecipeHelper() {}

    public static ImmutableList<BlockPos> normalizeFieldPositions(MiniaturizationFieldBlockData fieldBlocks) {
        return normalizeFieldPositions(fieldBlocks.getFilledBlocks(), fieldBlocks.getFilledBounds());
    }

    public static ImmutableList<BlockPos> normalizeFieldPositions(List<BlockPos> filledBlocks, AxisAlignedBB filledBounds) {
        return filledBlocks.stream()
                .parallel()
                .map(p -> BlockSpaceUtil.normalizeLayerPosition(filledBounds, p))
                .map(BlockPos::immutable)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Assumes an array is on the Z axis; meant to convert a single array to a map for collective
     * adding.
     *
     * @param array
     * @return
     */
    public static Map<BlockPos, String> convertSingleArrayToMap(String[] array, int x) {
        Map<BlockPos, String> map = new HashMap<>();
        for (int z = 0; z < array.length; z++) {
            String val = array[z];
            BlockPos relative = new BlockPos(x, 0, z);
            map.put(relative, val);
        }

        return map;
    }

    public static Map<BlockPos, String> convertMultiArrayToMap(String[][] array) {
        Map<BlockPos, String> map = new HashMap<>();

        // Loop through top level
        for (int x = 0; x < array.length; x++) {
            String[] zValues = array[x];
            map.putAll(convertSingleArrayToMap(zValues, x));
        }

        return map;
    }

    public static Map<String, Integer> getComponentCounts(Map<BlockPos, String> map) {
        Map<String, Integer> counts = new HashMap<>();

        for (String value : map.values()) {
            counts.merge(value, 0, Integer::sum);
        }

        return counts;
    }

    public static Set<MiniaturizationRecipe> getLoadedRecipes(World world) {
        if (world == null)
            return ImmutableSet.of();

        return world.getRecipeManager().getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE)
                .stream()
                .map(MiniaturizationRecipe.class::cast)
                .collect(Collectors.toSet());
    }

    public static Set<MiniaturizationRecipe> getLoadedRecipesThatFitField(World world, MiniaturizationFieldBlockData fieldBlocks) {
        Set<MiniaturizationRecipe> possibleRecipes = getLoadedRecipes(world);
        ImmutableList<BlockPos> relativeFilledBlocks = fieldBlocks.getRelativeFilledBlocks();
        IntSummaryStatistics[] stats = BlockSpaceUtil.getBlockPosStats(relativeFilledBlocks);
        BlockPos sizes = BlockSpaceUtil.getMaxBlockPos(stats).subtract(BlockSpaceUtil.getMinBlockPos(stats));
        AxisAlignedBB bounds = AxisAlignedBB.ofSize(sizes.getX() + 1, sizes.getY() + 1, sizes.getZ() + 1);
        possibleRecipes.removeIf(recipe -> !recipe.fitsInDimensions(bounds));
        return possibleRecipes;
    }
}
