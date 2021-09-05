package dev.compactmods.crafting.recipes.layers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class MixedComponentRecipeLayer implements IRecipeLayer, IFixedSizedRecipeLayer {
    private AxisAlignedBB dimensions;
    private RecipeLayerComponentPositionLookup componentLookup;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            RecipeLayerComponentPositionLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this.dimensions = AxisAlignedBB.ofSize(0, 0, 0);
        this.componentLookup = new RecipeLayerComponentPositionLookup();
    }

    public MixedComponentRecipeLayer(RecipeLayerComponentPositionLookup components) {
        this.componentLookup = components;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public RecipeLayerComponentPositionLookup getComponentLookup() {
        return this.componentLookup;
    }

    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    @Override
    public Set<String> getComponents() {
        return ImmutableSet.copyOf(componentLookup.getComponents());
    }

    public Map<String, Integer> getComponentTotals() {
        return componentLookup.getComponentTotals();
    }

    /**
     * Gets a component key for the given (normalized) position.
     *
     * @param pos
     * @return
     */
    public Optional<String> getComponentForPosition(BlockPos pos) {
        return componentLookup.getRequiredComponentKeyForPosition(pos);
    }

    public int getNumberFilledPositions() {
        return getComponentTotals()
                .values()
                .stream()
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean matches(IRecipeComponents components, IRecipeLayerBlocks blocks) {
        if(!blocks.allIdentified()) return false;

        return componentLookup.stream()
                .allMatch(e -> blocks.getComponentAtPosition(e.getKey())
                    .map(e.getValue()::equals)
                    .orElse(false));
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.MIXED_LAYER_TYPE.get();
    }
}
