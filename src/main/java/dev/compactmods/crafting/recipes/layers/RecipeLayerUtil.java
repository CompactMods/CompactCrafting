package dev.compactmods.crafting.recipes.layers;

import java.util.*;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public final class RecipeLayerUtil {

    private RecipeLayerUtil() {
    }

    public static IRecipeBlocks rotate(IRecipeBlocks original, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return new RecipeBlocks(original);
        }

        BlockPos[] originalPositions = original.getPositions().map(BlockPos::immutable).toArray(BlockPos[]::new);
        Map<BlockPos, BlockPos> rotatedPositions = BlockSpaceUtil.rotatePositionsInPlace(originalPositions, rotation);

        Map<BlockPos, BlockState> states = new HashMap<>();
        Map<BlockPos, String> componentKeys = new HashMap<>();
        Set<BlockPos> unmatchedPositions = new HashSet<>();

        for (BlockPos originalPos : originalPositions) {
            BlockPos rotatedPos = rotatedPositions.get(originalPos);

            // Add original block state from world info
            states.put(rotatedPos, original.getStateAtPosition(originalPos));

            // Copy over the matched component key to the new position
            original.getComponentAtPosition(originalPos).ifPresent(m -> componentKeys.put(rotatedPos, m));
        }

        if (!original.allIdentified()) {
            original.getUnmappedPositions()
                    .forEach(pos -> {
                        BlockPos rotated = rotatedPositions.get(pos).immutable();
                        unmatchedPositions.add(rotated);

                        original.getComponentAtPosition(pos).ifPresent(k -> componentKeys.put(rotated, k));
                    });
        }

        return new RecipeBlocks(original.getSourceBounds(), states, componentKeys, unmatchedPositions);
    }
}
