package com.robotgryphon.compactcrafting.recipes.layers;

import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class RecipeLayerUtil {

    private RecipeLayerUtil() {
    }

    public static IRecipeLayerBlocks rotate(IRecipeLayerBlocks original, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return new RecipeLayerBlocks(original);
        }

        BlockPos[] originalPositions = original.getPositions().toArray(BlockPos[]::new);
        Map<BlockPos, BlockPos> rotatedPositions = BlockSpaceUtil.rotatePositionsInPlace(originalPositions, rotation);

        Map<BlockPos, BlockState> states = new HashMap<>();
        Map<BlockPos, String> componentKeys = new HashMap<>();
        Set<BlockPos> unmatchedPositions = new HashSet<>();

        for (BlockPos originalPos : rotatedPositions.keySet()) {
            BlockPos rotatedPos = rotatedPositions.get(originalPos);

            // Add original block state from world info
            Optional<BlockState> state = original.getStateAtPosition(originalPos);
            state.ifPresent(blockState -> states.put(rotatedPos, blockState));

            // Copy over the matched component key to the new position
            Optional<String> matchedComp = original.getComponentAtPosition(originalPos);
            if(matchedComp.isPresent())
                componentKeys.put(rotatedPos, matchedComp.get());
            else
                unmatchedPositions.add(rotatedPos);
        }

        return new RecipeLayerBlocks(original.getBounds(), states, componentKeys, unmatchedPositions);
    }
}
