package com.robotgryphon.compactcrafting.recipes.components;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RecipeBlockStateComponent extends RecipeComponent {
    private final Block block;
    private BlockState blockState;
    private final Map<String, Predicate<Comparable<?>>> filters;
    private final Map<String, List<String>> allowedValues;
    public static final Codec<RecipeBlockStateComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(RecipeBlockStateComponent::getBlockName),
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("properties").forGetter(RecipeBlockStateComponent::getProperties)
    ).apply(i, RecipeBlockStateComponent::new));

    private Optional<Map<String, List<String>>> getProperties() {
        return Optional.of(allowedValues);
    }

    private ResourceLocation getBlockName() {
        return block.getRegistryName();
    }

    public RecipeBlockStateComponent(BlockState state) {
        this(state.getBlock());

        state.getValues().forEach((prop, comp) -> {
            this.filters.put(prop.getName(), comp::equals);
            this.allowedValues.put(prop.getName(), Lists.newArrayList(((Property) prop).getName(comp)));
        });
    }

    public RecipeBlockStateComponent(Block b) {
        this.block = b;
        this.filters = new HashMap<>();
        this.allowedValues = new HashMap<>();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // yes I know
    public RecipeBlockStateComponent(ResourceLocation blockId, Optional<Map<String, List<String>>> propertyRequirements) {
        this.block = ForgeRegistries.BLOCKS.getValue(blockId);
        if (this.block == null)
            throw new IllegalArgumentException("Block identifier does not exist.");

        this.filters = new HashMap<>();
        this.allowedValues = new HashMap<>();

        propertyRequirements.ifPresent(userRequestedValues -> {
            StateContainer<Block, BlockState> stateContainer = this.block.getStateDefinition();
            for (Map.Entry<String, List<String>> entry : userRequestedValues.entrySet()) {
                String propertyName = entry.getKey();
                List<String> userFilteredProps = entry.getValue();

                Property<?> prop = stateContainer.getProperty(propertyName);

                if (prop != null) {
                    List<Object> userAllowed = new ArrayList<>();
                    List<String> propertyAcceptableValues = new ArrayList<>();
                    for (String userValue : userFilteredProps) {
                        prop.getValue(userValue).ifPresent(u -> {
                            // We keep two values here - the actual property value for comparison,
                            // and the string value the user provided (for re-serialization in the CODEC)
                             propertyAcceptableValues.add(userValue);
                             userAllowed.add(u);
                        });
                    }

                    this.allowedValues.put(propertyName, propertyAcceptableValues);
                    this.filters.put(propertyName, userAllowed::contains);
                } else {
                    CompactCrafting.LOGGER.warn("Not a valid property: " + propertyName);
                }
            }
        });
    }

    public void setFilter(String property, Predicate<Comparable<?>> val) {
        // Check property exists by name
        Property<?> property1 = block.getStateDefinition().getProperty(property);
        if (property1 == null)
            throw new IllegalArgumentException(property);

        // Property exists in state container, we're good
        Collection<?> allowedValues = property1.getPossibleValues();
        boolean anyMatch = allowedValues.stream().anyMatch(v -> val.test((Comparable<?>) v));
        if (!anyMatch) {
            CompactCrafting.LOGGER.warn("Failed to allow filter: No values would be valid for property [{}]", property);
            return;
        }

        filters.put(property, val);
    }

    public boolean filterMatches(BlockState state) {
        if(state.getBlock().getRegistryName() != this.block.getRegistryName())
            return false;

        for (Property<?> prop : state.getProperties()) {
            String name = prop.getName();

            // If it's not in the whitelist, we don't care about what the value is
            if (!filters.containsKey(name))
                continue;

            Comparable<?> val = state.getValue(prop);
            boolean matches = filters.get(name).test(val);
            if (!matches) return false;
        }

        return true;
    }

    @Override
    public RecipeComponentType<?> getType() {
        return Registration.BLOCKSTATE_COMPONENT.get();
    }

    public Block getBlock() {
        return this.block;
    }

    /**
     * Get a state with any directions that are only one possible value already applied.
     *
     * @return
     */
    public BlockState getStateWithDirections() {
        if (this.blockState == null) {
            this.blockState = this.block.defaultBlockState();
            List<DirectionProperty> dirProperties = this.blockState.getProperties().stream()
                    .filter(DirectionProperty.class::isInstance)
                    .map(DirectionProperty.class::cast)
                    .collect(Collectors.toList());
            if (dirProperties.isEmpty())
                return this.blockState;

            for (DirectionProperty prop : dirProperties) {
                List<String> allowed = this.allowedValues.get(prop.getName());
                if (allowed != null && allowed.size() == 1) {
                    Optional<Direction> opt = prop.getValue(allowed.get(0));
                    if (opt.isPresent())
                        this.blockState = this.blockState.setValue(prop, opt.get());
                }
            }
        }

        return this.blockState;
    }
}
