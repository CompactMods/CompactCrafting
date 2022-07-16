package dev.compactmods.crafting.recipes.layers;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.ISymmetricalLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.core.CCLayerTypes;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class HollowComponentRecipeLayer implements IRecipeLayer, IDynamicSizedRecipeLayer, ISymmetricalLayer,
        RecipeLayerType<HollowComponentRecipeLayer> {

    private final String componentKey;
    private AABB recipeDimensions;
    private Set<BlockPos> filledPositions;

    public static final Codec<HollowComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("wall").forGetter(HollowComponentRecipeLayer::getComponent)
    ).apply(i, HollowComponentRecipeLayer::new));

    public HollowComponentRecipeLayer() {
        this.componentKey = null;
        this.filledPositions = Collections.emptySet();
    }

    public HollowComponentRecipeLayer(String component) {
        this.componentKey = component;
        this.filledPositions = Collections.emptySet();
    }

    @Override
    public RecipeLayerType<?> getType() {
        return CCLayerTypes.HOLLOW_LAYER_TYPE.get();
    }

    @Override
    public Set<String> getComponents() {
        return ImmutableSet.of(componentKey);
    }

    public Map<String, Integer> getComponentTotals() {
        return Collections.singletonMap(componentKey, getNumberFilledPositions());
    }

    public Optional<String> getComponentForPosition(BlockPos pos) {
        if (filledPositions.contains(pos))
            return Optional.ofNullable(componentKey);

        return Optional.empty();
    }

    @Override
    public Stream<BlockPos> getPositionsForComponent(String component) {
        if (!component.equals(componentKey))
            return Stream.empty();

        return filledPositions.stream();
    }

    public int getNumberFilledPositions() {
        return filledPositions.size();
    }

    @Override
    public boolean matches(IRecipeComponents components, IRecipeBlocks blocks) {
        if (!blocks.allIdentified()) {
            boolean anyNonAir = blocks.getUnmappedPositions()
                    .map(blocks::getStateAtPosition)
                    .anyMatch(state -> !state.isAir());

            // Blocks that were not identified are not air - fail the layer match
            if (anyNonAir) return false;
        }

        Map<String, Integer> totalsInWorld = blocks.getKnownComponentTotals();

        // If we don't have any of the wall components, immediately fail
        if (!totalsInWorld.containsKey(this.componentKey))
            return false;

        // Hollow layers only match a single component type
        if (totalsInWorld.size() > 1) {
            // make sure all the matched components besides the wall are empty
            boolean everythingElseEmpty = totalsInWorld.keySet()
                    .stream()
                    // exclude this component (wall) from empty matching
                    .filter(c -> !c.equals(this.componentKey))
                    .allMatch(components::isEmptyBlock);

            if (!everythingElseEmpty) return false;
        }

        int targetCount = totalsInWorld.get(componentKey);
        int layerCount = filledPositions.size();

        return layerCount == targetCount;
    }

    @Override
    public void setRecipeDimensions(AABB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    @Override
    public void recalculateRequirements() {
        this.filledPositions = BlockSpaceUtil.getWallPositions(recipeDimensions)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    public String getComponent() {
        return this.componentKey;
    }

    @Override
    public Codec<HollowComponentRecipeLayer> getCodec() {
        return CODEC;
    }
}
