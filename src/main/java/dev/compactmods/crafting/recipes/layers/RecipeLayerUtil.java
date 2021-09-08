package dev.compactmods.crafting.recipes.layers;

import java.util.*;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public final class RecipeLayerUtil {

    private RecipeLayerUtil() {
    }

    public static IRecipeLayerBlocks rotate(IRecipeLayerBlocks original, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return new RecipeLayerBlocks(original);
        }

        BlockPos[] originalPositions = original.getPositions().map(BlockPos::immutable).toArray(BlockPos[]::new);
        Map<BlockPos, BlockPos> rotatedPositions = BlockSpaceUtil.rotatePositionsInPlace(originalPositions, rotation);

        Map<BlockPos, BlockState> states = new HashMap<>();
        Map<BlockPos, String> componentKeys = new HashMap<>();
        Set<BlockPos> unmatchedPositions = new HashSet<>();

        for (BlockPos originalPos : originalPositions) {
            BlockPos rotatedPos = rotatedPositions.get(originalPos);

            // Add original block state from world info
            Optional<BlockState> state = original.getStateAtPosition(originalPos);
            state.ifPresent(blockState -> states.put(rotatedPos, blockState));
            if (!state.isPresent()) {
                unmatchedPositions.add(rotatedPos);
            }

            // Copy over the matched component key to the new position
            original.getComponentAtPosition(originalPos).ifPresent(m -> componentKeys.put(rotatedPos, m));
        }

        return new RecipeLayerBlocks(original.getBounds(), states, componentKeys, unmatchedPositions);
    }
}
