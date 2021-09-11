package dev.compactmods.crafting.recipes.blocks;

import java.util.*;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class RecipeLayerBlocks implements IRecipeLayerBlocks {

    private final AxisAlignedBB layerBounds;
    private final ComponentPositionLookup lookup;
    private final Map<BlockPos, BlockState> states;
    private final Set<BlockPos> unmatchedStates;

    protected RecipeLayerBlocks(AxisAlignedBB bounds) {
        layerBounds = bounds;
        lookup = new ComponentPositionLookup();
        states = new HashMap<>();
        unmatchedStates = new HashSet<>();
    }

    public RecipeLayerBlocks(IRecipeLayerBlocks original) {
        this(original.getBounds());

        // Loops all block positions inside bounds, for-each
        BlockPos.betweenClosedStream(layerBounds).forEach(pos -> {
            Optional<String> key = original.getComponentAtPosition(pos);
            if (key.isPresent()) lookup.components.put(pos.immutable(), key.get());
            else unmatchedStates.add(pos.immutable());

            BlockState oState = original.getStateAtPosition(pos);
            states.put(pos.immutable(), oState);
        });
    }

    public RecipeLayerBlocks(AxisAlignedBB bounds, Map<BlockPos, BlockState> states,
                             Map<BlockPos, String> components, Set<BlockPos> unmatchedStates) {
        this(bounds);
        this.states.putAll(states);
        lookup.components.putAll(components);
        this.unmatchedStates.addAll(unmatchedStates);

        this.lookup.rebuildComponentTotals();
    }

    public static RecipeLayerBlocks create(IBlockReader blocks, MiniaturizationRecipe recipe, AxisAlignedBB bounds) {
        RecipeLayerBlocks instance = new RecipeLayerBlocks(bounds);

        BlockSpaceUtil.getBlocksIn(bounds).forEach(pos -> {
            BlockState state = blocks.getBlockState(pos);

            BlockPos normalizedPos = BlockSpaceUtil.normalizeLayerPosition(bounds, pos);

            instance.states.put(normalizedPos, state);

            // Pre-populate a set of component keys from the recipe instance, so we don't have to do it later
            Optional<String> compKey = recipe.getRecipeComponentKey(state);
            if (compKey.isPresent())
                instance.lookup.add(normalizedPos, compKey.get());
            else
                instance.unmatchedStates.add(normalizedPos);

        });

        return instance;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.layerBounds;
    }

    @Override
    public boolean allIdentified() {
        return unmatchedStates.isEmpty();
    }

    @Override
    public Stream<BlockPos> getUnmappedPositions() {
        return unmatchedStates.stream();
    }

    @Override
    public Stream<String> getUnknownComponents() {
        return getUnmappedPositions()
                .map(lookup::getRequiredComponentKeyForPosition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct();
    }

    @Override
    public Stream<BlockPos> getPositionsForComponent(String component) {
        return this.lookup.getPositionsForComponent(component);
    }


    @Override
    public Optional<String> getComponentAtPosition(BlockPos relative) {
        return Optional.ofNullable(lookup.components.get(relative));
    }

    @Override
    public BlockState getStateAtPosition(BlockPos relative) {
        return states.get(relative);
    }

    @Override
    public Stream<BlockPos> getPositions() {
        return states.keySet().stream();
    }

    /**
     * Gets the number of unique component keys in this set of blocks.
     *
     * @return
     */
    @Override
    public int getNumberKnownComponents() {
        return this.getKnownComponentTotals().keySet().size();
    }

    @Override
    public void rebuildComponentTotals() {
        lookup.rebuildComponentTotals();
    }

    @Override
    public Map<String, Integer> getKnownComponentTotals() {
        return lookup.componentTotals;
    }
}
