package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleComponentRecipeLayer implements IRecipeLayer {

    private String componentKey;
    private Vector3i dimensions;

    /**
     * Relative positions that are filled by the component.
     * Example:
     *
     *   X ----->
     * Z   X-X
     * |   -X-
     * |  -X-X
     *
     * = [0, 0], [2, 0], [1, 1], [0, 2], [2, 2]
     */
    private Set<BlockPos> filledPositions;

    public SingleComponentRecipeLayer(String key, Set<BlockPos> filledPositions) {
        this.componentKey = key;
        this.filledPositions = filledPositions;

        recalculateDimensions();
    }

    @Override
    public boolean matchesFieldLayer(IWorldReader world, MiniaturizationRecipe recipe, AxisAlignedBB fieldLayer) {
        Optional<BlockState> component = recipe.getRecipeComponent(componentKey);
        // We can't find a component definition in the recipe, so something very wrong happened
        if(!component.isPresent()) {
            CompactCrafting.LOGGER.warn(
                String.format("Attempted to find component with key '%s' but no component was found.", componentKey)
            );

            return false;
        }

        // Dimensions check - make sure the field layer is large enough for the recipe layer
        if(this.dimensions.getX() > fieldLayer.getXSize() || this.dimensions.getZ() > fieldLayer.getZSize())
            return false;

        // Non-empty field positions
        Set<BlockPos> fieldPositions = BlockPos.getAllInBox(fieldLayer)
                .filter(p -> !world.isAirBlock(p))
                .map(BlockPos::toImmutable)
                .collect(Collectors.toSet());

        // Get all the distinct block types in the current layer
        Stream<BlockState> distinctStates = fieldPositions.stream()
                .map(world::getBlockState)
                .distinct();

        // Invalid block found - early exit
        if(distinctStates.anyMatch(state -> state != component.get()))
            return false;

        // Normalize the block positions so the recipe can match easier
        BlockPos[] normalizedFilledPositions = fieldPositions.stream()
                .map(p -> new BlockPos(
                        p.getX() - fieldLayer.minX,
                        p.getY() - fieldLayer.minY,
                        p.getZ() - fieldLayer.minZ
                ))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);


        // Create a minimum-filled bounds of blocks in the field
        MutableBoundingBox trimmedBounds = new MutableBoundingBox(normalizedFilledPositions[0], normalizedFilledPositions[0]);
        for(BlockPos filledPos : normalizedFilledPositions) {
            MutableBoundingBox p = new MutableBoundingBox(filledPos, filledPos);

            if(!trimmedBounds.intersectsWith(p))
                trimmedBounds.expandTo(p);
        }

        // Whitespace trim done - no padding needed, min and max bounds are already correct

        // Check recipe template against padded world layout



        // Finally, simply check the normalized template
        return Arrays.stream(normalizedFilledPositions)
                .parallel()
                .allMatch(np -> {
                    Vector3i reAdjusted = np.subtract(new Vector3i(trimmedBounds.minX, trimmedBounds.minY, trimmedBounds.minZ));
                    return this.filledPositions.contains(reAdjusted);
                });
    }

    private Vector3i recalculateDimensions() {
        this.dimensions = new Vector3i(1, 1, 1);
        return this.dimensions;
    }

    @Override
    public Vector3i getDimensions() {
        return dimensions;
    }

    @Override
    public Vector3i getRelativeOffset() {
        return Vector3i.NULL_VECTOR;
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        int volume = dimensions.getX() * dimensions.getY() * dimensions.getZ();
        return Collections.singletonMap(componentKey, volume);
    }
}
