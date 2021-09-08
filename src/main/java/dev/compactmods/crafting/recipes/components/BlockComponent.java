package dev.compactmods.crafting.recipes.components;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.util.CodecExtensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

public class BlockComponent implements IRecipeComponent, IRecipeBlockComponent {

    private final Block block;
    private final Set<BlockState> validStates;
    private boolean erroredRendering = false;
    private final Map<String, Predicate<Comparable<?>>> filters;
    private final HashMap<String, List<String>> allowedValues;

    public static final Codec<BlockComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.BLOCK_ID_CODEC.fieldOf("block").forGetter(BlockComponent::getBlock),
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("properties").forGetter(BlockComponent::getProperties)
    ).apply(i, BlockComponent::new));

    private Optional<Map<String, List<String>>> getProperties() {
        return Optional.of(allowedValues);
    }

    public BlockComponent(Block block) {
        this.block = block;
        this.filters = new HashMap<>();
        this.allowedValues = new HashMap<>();
        this.validStates = new HashSet<>();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // yes I know
    public BlockComponent(Block block, Optional<Map<String, List<String>>> propertyRequirements) {
        this(block);

        StateContainer<Block, BlockState> stateContainer = this.block.getStateDefinition();
        propertyRequirements.ifPresent(userRequestedValues -> {
            for (Map.Entry<String, List<String>> userPropFilter : userRequestedValues.entrySet()) {
                validateAndAddUserFilter(stateContainer, userPropFilter);
            }
        });

        final Set<BlockState> valid = block.getStateDefinition()
                .getPossibleStates()
                .stream()
                .filter(this::matches)
                .collect(Collectors.toSet());

        this.validStates.addAll(valid);
    }

    private void validateAndAddUserFilter(StateContainer<Block, BlockState> stateContainer, Map.Entry<String, List<String>> userPropFilter) {
        String propertyName = userPropFilter.getKey();

        Property<?> prop = stateContainer.getProperty(propertyName);
        if (prop == null) {
            CompactCrafting.RECIPE_LOGGER.warn("Not a valid property: " + propertyName);
            return;
        }

        List<Object> userAllowed = new ArrayList<>();
        List<String> propertyAcceptableValues = new ArrayList<>();
        for (String userValue : userPropFilter.getValue()) {
            prop.getValue(userValue).ifPresent(u -> {
                // We keep two values here - the actual property value for comparison,
                // and the string value the user provided (for re-serialization in the CODEC)
                propertyAcceptableValues.add(userValue);
                userAllowed.add(u);
            });
        }

        this.allowedValues.put(propertyName, propertyAcceptableValues);
        this.filters.put(propertyName, userAllowed::contains);
    }

    public boolean matches(BlockState state) {
        if (!state.getBlock().equals(this.block))
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
        return ComponentRegistration.BLOCK_COMPONENT.get();
    }

    public Block getBlock() {
        return this.block;
    }

    @Override
    public BlockState getRenderState() {
        return block.defaultBlockState();
    }

    @Override
    public boolean didErrorRendering() {
        return erroredRendering;
    }

    @Override
    public void markRenderingErrored() {
        erroredRendering = true;
    }

    public boolean hasFilter(String property) {
        return filters.containsKey(property);
    }

    public Optional<BlockState> getFirstMatch() {
        return validStates.isEmpty() ? Optional.empty() : validStates.stream().findFirst();
    }
}
