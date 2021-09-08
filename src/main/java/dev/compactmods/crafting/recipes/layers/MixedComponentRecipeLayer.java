package dev.compactmods.crafting.recipes.layers;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class MixedComponentRecipeLayer implements IRecipeLayer, IFixedSizedRecipeLayer {
    private AxisAlignedBB dimensions;
    private ComponentPositionLookup componentLookup;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            ComponentPositionLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this.dimensions = AxisAlignedBB.ofSize(0, 0, 0);
        this.componentLookup = new ComponentPositionLookup();
    }

    public MixedComponentRecipeLayer(ComponentPositionLookup components) {
        this.componentLookup = components;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public ComponentPositionLookup getComponentLookup() {
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

    @Override
    public Stream<BlockPos> getPositionsForComponent(String component) {
        return componentLookup.getPositionsForComponent(component);
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

        final Collection<String> requiredKeys = this.componentLookup.getComponents();
        final Map<String, Integer> componentTotals = blocks.getKnownComponentTotals();

        if(requiredKeys.size() != componentTotals.size())
            return false;

        // Dry run that all the components required are in the component list
        boolean missingRequired = requiredKeys.stream().anyMatch(rk -> !componentTotals.containsKey(rk));
        if(missingRequired) {
            if(ServerConfig.RECIPE_MATCHING.get())
                CompactCrafting.RECIPE_LOGGER.debug("Failed to match: required components are missing.");

            return false;
        }

        for(String required : requiredKeys) {
            final IRecipeBlockComponent block = components.getBlock(required).orElse(null);

            final Set<BlockPos> actual = blocks.getPositionsForComponent(required)
                    .map(BlockPos::immutable).collect(Collectors.toSet());
            final Set<BlockPos> expected = componentLookup.getPositionsForComponent(required)
                    .map(BlockPos::immutable).collect(Collectors.toSet());

            // Dry run - ensure component counts match actual v. expected
            if(expected.size() != actual.size()) {
                if(ServerConfig.RECIPE_MATCHING.get())
                    CompactCrafting.RECIPE_LOGGER.debug("Failed to match: required vs. expected counts do not match. ");
                return false;
            }

            if(!expected.equals(actual))
            {
                if(ServerConfig.RECIPE_MATCHING.get())
                    CompactCrafting.RECIPE_LOGGER.debug("Failed to match: required components are missing.");
                return false;
            }
        }

        return true;
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.MIXED_LAYER_TYPE.get();
    }
}
