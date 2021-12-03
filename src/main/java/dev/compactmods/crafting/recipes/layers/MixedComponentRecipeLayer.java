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
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MixedComponentRecipeLayer implements IRecipeLayer, IFixedSizedRecipeLayer {
    private final AABB dimensions;
    private final ComponentPositionLookup componentLookup;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            ComponentPositionLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this.dimensions = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
        this.componentLookup = new ComponentPositionLookup();
    }

    public MixedComponentRecipeLayer(ComponentPositionLookup components) {
        this.componentLookup = components;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public ComponentPositionLookup getComponentLookup() {
        return this.componentLookup;
    }

    public AABB getDimensions() {
        return this.dimensions;
    }

    @Override
    public Set<String> getComponents() {
        return ImmutableSet.copyOf(componentLookup.getComponents());
    }

    @Override
    public void dropNonRequiredComponents(IRecipeComponents components) {
        components.getEmptyComponents().forEach(componentLookup::remove);

        final Collection<String> definedKeys = components.getBlockComponents().keySet();
        final Set<String> toRemove = componentLookup.getComponents()
                .stream()
                .filter(layerComp -> !definedKeys.contains(layerComp))
                .collect(Collectors.toSet());

        if(ServerConfig.RECIPE_REGISTRATION.get())
            CompactCrafting.RECIPE_LOGGER.debug(
                    "Removing {} from required component list; it was not defined in the recipe.",
                    String.join(",", toRemove)
            );

        toRemove.forEach(componentLookup::remove);
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
    public boolean matches(IRecipeComponents components, IRecipeBlocks blocks) {
        if (!blocks.allIdentified()) {
            boolean anyNonAir = blocks.getUnmappedPositions()
                    .map(blocks::getStateAtPosition)
                    .anyMatch(state -> !state.isAir());

            // Blocks that were not identified are not air - fail the layer match
            if (anyNonAir) return false;
        }

        final Collection<String> requiredKeys = this.componentLookup.getComponents();
        final Map<String, Integer> componentTotals = blocks.getKnownComponentTotals();

        // Dry run that all the components required are in the component list
        Set<String> missingRequired = requiredKeys.stream().filter(rk -> !componentTotals.containsKey(rk)).collect(Collectors.toSet());
        if(!missingRequired.isEmpty()) {
            if(ServerConfig.RECIPE_MATCHING.get())
                CompactCrafting.RECIPE_LOGGER.debug("Failed to match: required components ({}) are missing.", String.join(",", missingRequired));

            return false;
        }

        for(String required : requiredKeys) {
            final IRecipeBlockComponent block = components.getBlock(required).orElse(null);

            final Set<BlockPos> actual = blocks.getPositionsForComponent(required)
                    .map(BlockPos::immutable).collect(Collectors.toSet());

            final Set<BlockPos> expected = componentLookup.getPositionsForComponent(required)
                    .map(BlockPos::immutable).collect(Collectors.toSet());

            if(!expected.equals(actual))
            {
                if(ServerConfig.RECIPE_MATCHING.get())
                    CompactCrafting.RECIPE_LOGGER.debug("Failed to match: required components are missing or in incorrect spots.");
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
