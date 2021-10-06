package dev.compactmods.crafting.recipes.blocks;

import java.util.*;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.Template;

public class RecipeBlocks implements IRecipeBlocks {

    private AxisAlignedBB sourceBounds;
    private AxisAlignedBB filledBounds;
    private final ComponentPositionLookup lookup;
    private final Map<BlockPos, BlockState> states;
    private final Set<BlockPos> unmatchedStates;

    protected RecipeBlocks(AxisAlignedBB bounds) {
        this.sourceBounds = bounds;
        this.filledBounds = BlockSpaceUtil.getBoundsForBlocks(BlockSpaceUtil.getBlocksIn(bounds));
        lookup = new ComponentPositionLookup();
        states = new HashMap<>();
        unmatchedStates = new HashSet<>();
    }

    public RecipeBlocks(IRecipeBlocks original) {
        this(original.getSourceBounds());
        copyInfoFrom(original, BlockPos.ZERO);
    }

    public static RecipeBlocks createEmpty() {
        return new RecipeBlocks(AxisAlignedBB.ofSize(0, 0, 0));
    }

    private void copyInfoFrom(IRecipeBlocks original, BlockPos offset) {
        // Loops all block positions inside bounds, for-each
        BlockSpaceUtil.getBlocksIn(sourceBounds).forEach(newPos -> {
            Optional<String> key = original.getComponentAtPosition(newPos);
            if (key.isPresent()) lookup.add(newPos.offset(offset).immutable(), key.get());
            else {
                final BlockState unidentified = original.getStateAtPosition(newPos);
                if (!unidentified.isAir())
                    unmatchedStates.add(newPos.offset(offset).immutable());
            }

            BlockState oState = original.getStateAtPosition(newPos);
            states.put(newPos.offset(offset).immutable(), oState);
        });

        rebuildComponentTotals();
    }

    public RecipeBlocks(AxisAlignedBB bounds, Map<BlockPos, BlockState> states,
                        Map<BlockPos, String> components, Set<BlockPos> unmatchedStates) {
        this(bounds);
        this.states.putAll(states);
        lookup.components.putAll(components);
        this.unmatchedStates.addAll(unmatchedStates);

        this.lookup.rebuildComponentTotals();
    }

    public static RecipeBlocks create(IBlockReader blocks, IRecipeComponents components, AxisAlignedBB bounds) {
        RecipeBlocks instance = new RecipeBlocks(bounds);

        BlockSpaceUtil.getBlocksIn(bounds).map(BlockPos::immutable).forEach(pos -> {
            BlockState state = blocks.getBlockState(pos);

            // BlockPos normalizedPos = BlockSpaceUtil.normalizeLayerPosition(bounds, pos);

            instance.states.put(pos, state);

            // Pre-populate a set of component keys from the recipe instance, so we don't have to do it later
            Optional<String> compKey = components.getKey(state);
            if (compKey.isPresent())
                instance.lookup.add(pos, compKey.get());
            else
                instance.unmatchedStates.add(pos);

        });

        return instance;
    }

    @Override
    public AxisAlignedBB getSourceBounds() {
        return this.sourceBounds;
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
    public Stream<BlockPos> getPositionsForComponent(String component) {
        return this.lookup.getPositionsForComponent(component);
    }

    @Override
    public AxisAlignedBB getFilledBounds() {
        return this.filledBounds;
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

    @Override
    public IRecipeBlocks slice(AxisAlignedBB bounds) {
        final RecipeBlocks blocks = new RecipeBlocks(sourceBounds.intersect(bounds));
        blocks.copyInfoFrom(this, BlockPos.ZERO);
        return blocks;
    }

    @Override
    public IRecipeBlocks offset(Vector3i amount) {
        final RecipeBlocks copy = new RecipeBlocks(this.filledBounds);
        copy.copyInfoFrom(this, new BlockPos(amount));

        copy.filledBounds = copy.filledBounds.move(new Vector3d(amount.getX(), amount.getY(), amount.getZ()));
        copy.sourceBounds = copy.sourceBounds.move(new BlockPos(amount));

//        final Map<BlockPos, BlockState> statesRelocated = states.entrySet().stream()
//                .map(e -> Pair.of(e.getKey().offset(amount), e.getValue()))
//                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
//
//        copy.states.clear();
//        copy.states.putAll(statesRelocated);
//
//        final Set<BlockPos> unmatched = unmatchedStates.stream()
//                .map(b -> b.offset(amount))
//                .map(BlockPos::immutable)
//                .collect(Collectors.toSet());
//
//        copy.unmatchedStates.clear();
//        copy.unmatchedStates.addAll(unmatched);
//
//        final Map<BlockPos, String> relocated = copy.lookup.components.entrySet()
//                .stream()
//                .map(e -> Pair.of(e.getKey().offset(amount), e.getValue()))
//                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
//
//        copy.lookup.components.clear();
//        copy.lookup.components.putAll(relocated);

        return copy;
    }

    public Template toTemplate() {
        return null;
    }
}
