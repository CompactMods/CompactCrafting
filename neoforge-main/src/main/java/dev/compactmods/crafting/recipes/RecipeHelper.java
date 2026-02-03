package dev.compactmods.crafting.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class RecipeHelper {

    @SuppressWarnings("unchecked")
    public static Optional<RecipeHolder<MiniaturizationRecipe>> getRecipe(LevelAccessor levelAccessor, Identifier recipeId) {
        var recipes = Objects.requireNonNull(levelAccessor.getServer())
                .getRecipeManager();

        final var key = ResourceKey.create(Registries.RECIPE, recipeId);

        return recipes.byKey(key)
                .filter(recipeHolder -> recipeHolder.value() instanceof MiniaturizationRecipe)
                .map(holder -> (RecipeHolder<MiniaturizationRecipe>) holder);
    }

    /// Assumes an array is on the Z axis; meant to convert a single array to a map for collective adding.
    public static Map<BlockPos, String> convertSingleArrayToMap(String[] array, int x) {
        HashMap<BlockPos, String> map = new HashMap<>();
        for (int z = 0; z < array.length; z++) {
            String val = array[z];
            BlockPos relative = new BlockPos(x, 0, z);
            map.put(relative, val);
        }

        return map;
    }

    public static Map<BlockPos, String> convertMultiArrayToMap(String[][] array) {
        HashMap<BlockPos, String> map = new HashMap<>();

        // Loop through top level
        for (int x = 0; x < array.length; x++) {
            String[] zValues = array[x];
            map.putAll(convertSingleArrayToMap(zValues, x));
        }

        return map;
    }

    public static String[][] generateArrayFromBounds(AABB boundsForBlocks) {
        int zMax = (int) boundsForBlocks.getZsize();
        String[][] map = new String[(int) boundsForBlocks.getXsize()][];
        for(int x = 0; x < boundsForBlocks.getXsize(); x++) map[x] = new String[zMax];

        return map;
    }

}
