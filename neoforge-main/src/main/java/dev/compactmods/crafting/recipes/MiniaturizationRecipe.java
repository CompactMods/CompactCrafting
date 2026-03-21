package dev.compactmods.crafting.recipes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.components.IPositionalComponentLookup;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.ISymmetricalLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MiniaturizationRecipe(
        TreeMap<Integer, IRecipeLayer> layers,
        ItemPredicate catalystMatcher,
        ItemStackTemplate[] outputs,
        AABB dimensions,
        int requiredTime,
        boolean hasFixedFootprint,
        Map<String, Integer> cachedComponentTotals,
        MiniaturizationRecipeComponents components
) implements IMiniaturizationRecipe {

    public static final Codec<IRecipeLayer> LAYER_CODEC = Codec.lazyInitialized(() -> {
        final var reg = CompactCraftingCommon.RECIPE_LAYER_TYPES_REGISTRY.byNameCodec();
        return reg.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);
    });

    public static final Codec<IRecipeComponent> COMPONENT_CODEC = CompactCraftingCommon.RECIPE_COMPONENTS_REGISTRY
            .byNameCodec()
            .dispatch(IRecipeComponent::getType, RecipeComponentType::getCodec);

    public static final MapCodec<MiniaturizationRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("craftingTime", 200)
                    .forGetter(MiniaturizationRecipe::getCraftingTime),

            Codec.INT.optionalFieldOf("recipeSize", -1)
                    .forGetter(MiniaturizationRecipe::codecRecipeSize),

            LAYER_CODEC.listOf().fieldOf("layers")
                    .forGetter(MiniaturizationRecipe::codecLayerList),

            MiniaturizationRecipeComponents.CODEC.optionalFieldOf("components", MiniaturizationRecipeComponents.EMPTY)
                    .forGetter(MiniaturizationRecipe::getComponents),

            ItemStackTemplate.CODEC.listOf().fieldOf("outputs")
                    .forGetter(MiniaturizationRecipe::codecOutputs),

            ItemPredicate.CODEC.fieldOf("catalyst")
                    .forGetter(MiniaturizationRecipe::catalystMatcher)

    ).apply(i, MiniaturizationRecipe::fromCodec));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiniaturizationRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MiniaturizationRecipe::requiredTime,
            ByteBufCodecs.INT, MiniaturizationRecipe::codecRecipeSize,
            ByteBufCodecs.fromCodec(LAYER_CODEC).apply(ByteBufCodecs.list()), MiniaturizationRecipe::codecLayerList,
            MiniaturizationRecipeComponents.STREAM_CODEC, MiniaturizationRecipe::components,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), MiniaturizationRecipe::codecOutputs,
            ByteBufCodecs.fromCodecWithRegistries(ItemPredicate.CODEC), MiniaturizationRecipe::catalystMatcher,
            MiniaturizationRecipe::fromCodec
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<MiniaturizationRecipe>> MINI_RECIPE_HOLDER_STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.RECIPE), RecipeHolder::id,
            MiniaturizationRecipe.STREAM_CODEC, RecipeHolder::value,
            RecipeHolder::new
    );

    public static MiniaturizationRecipe fromCodec(int craftTime, int recipeSize, List<IRecipeLayer> layers,
                                                  MiniaturizationRecipeComponents components, List<ItemStackTemplate> outputs,
                                                  ItemPredicate catalyst) {
        var layers1 = new TreeMap<Integer, IRecipeLayer>();

        // region Layers
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            layers1.put(y, rev.get(y));
        // endregion

        final var tempUnknownKeys = layers.stream()
                .map(IRecipeLayer::getComponents)
                .flatMap(Set::stream)
                .filter(key -> !components.isKnownKey(key))
                .collect(Collectors.toUnmodifiableSet());

        for (String key : tempUnknownKeys) {
            CompactCrafting.RECIPE_LOGGER.warn("Got layer-required component '{}' but it was not defined in the recipe; removing.", key);

            // Only supports fixed-size layers (particular example is mixed layers, which need to specify gaps)
            // Fluid level layers having unknown components is almost certainly a bug on the recipe author
            for (IRecipeLayer layer : layers) {
                if (layer instanceof IFixedSizedRecipeLayer fixedLayer) {
                    IPositionalComponentLookup p = fixedLayer.getComponentLookup();
                    p.remove(key);
                }
            }
        }

        int height = layers1.size();
        int x = 0;
        int z = 0;

        boolean hasFixedFootprint = layers1.values().stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        AABB recipeDims;
        if (!hasFixedFootprint) {
            if (recipeSize < 1) {
                CompactCrafting.RECIPE_LOGGER.warn("Warning: recipe dimensions are not strictly defined but recipeSize is not set. Forcing it to 1.");
                recipeSize = 1;
            }

            recipeDims = new AABB(0, 0, 0, recipeSize, height, recipeSize);
        } else {
            for (var l : layers1.values()) {
                // We only need to worry about fixed-level layers; the fluid layers will adapt
                if (l instanceof IFixedSizedRecipeLayer) {
                    AABB dimensions = ((IFixedSizedRecipeLayer) l).getDimensions();
                    if (dimensions.getXsize() > x)
                        x = (int) Math.ceil(dimensions.getXsize());

                    if (dimensions.getZsize() > z)
                        z = (int) Math.ceil(dimensions.getZsize());
                }
            }

            recipeDims = new AABB(Vec3.ZERO, new Vec3(x, height, z));
        }

        HashMap<String, Integer> componentTotals = new HashMap<>();
        components.getAllComponents().keySet().forEach(comp -> {
            int count = getComponentRequiredCount(comp, components, layers1);
            componentTotals.put(comp, count);
        });

        var recipe = new MiniaturizationRecipe(layers1, catalyst, outputs.toArray(ItemStackTemplate[]::new),
                recipeDims, craftTime, hasFixedFootprint,
                componentTotals, components);

        recipe.updateFluidLayerDimensions();
        return recipe;
    }

    private void updateFluidLayerDimensions() {
        // Update all the dynamic recipe layers
        final AABB footprint = BlockSpaceUtil.getLayerBounds(dimensions, 0);
        this.layers.values()
                .stream()
                .filter(l -> l instanceof IDynamicSizedRecipeLayer)
                .forEach(dl -> ((IDynamicSizedRecipeLayer) dl).setRecipeDimensions(footprint));
    }

    /// Checks that a given projectors size can contain this recipe.
    public boolean fitsInFieldSize(MiniaturizationFieldSize fieldSize) {
        int dim = fieldSize.getDimensions();
        return (dimensions.getXsize() <= dim) &&
                (dimensions.getYsize() <= dim) &&
                (dimensions.getZsize() <= dim);
    }

    public boolean matches(IRecipeBlocks blocks) {
        if (!BlockSpaceUtil.boundsFitsInside(blocks.getFilledBounds(), dimensions)) {
            return false;
        }

        // We know that the recipe will at least fit inside the current projection projectors
        AABB filledBounds = blocks.getFilledBounds();

        Rotation[] validRotations = Rotation.values();

        Map<Rotation, Set<Integer>> layerRotationMatches = new HashMap<>(validRotations.length);
        for (Rotation r : validRotations)
            layerRotationMatches.put(r, new HashSet<>(layers.size()));

        for (Map.Entry<Integer, IRecipeLayer> entry : layers.entrySet()) {
            IRecipeLayer layer = entry.getValue();
            IRecipeBlocks layerBlocks = blocks.slice(BlockSpaceUtil.getLayerBounds(blocks.getFilledBounds(), entry.getKey())).normalize();

            // If the layer spec requires all components to be known (by default) then check early
            if (layer.requiresAllBlocksIdentified() && !layerBlocks.allIdentified())
                return false;

            final boolean firstMatched = layer.matches(components, layerBlocks);
            if (firstMatched)
                layerRotationMatches.get(Rotation.NONE).add(entry.getKey());

            // Symmetrical layers require symmetric footprints
            // We could clean this up by doing 180 flips but extra math, we can fallback for now
            if (layer instanceof ISymmetricalLayer && (dimensions.getXsize() == dimensions.getZsize())) {
                if (!firstMatched) {
                    CompactCrafting.RECIPE_LOGGER.debug("Failing recipe layer {}; marked symmetrical and does not match its first rotation attempt.", entry.getKey());

                    // Immediate fail of recipe - no other matches are possible here
                    return false;
                } else {
                    // Fill all possible rotations for layer; symmetric layers will match all of them
                    for (Rotation r : validRotations) {
                        if (r != Rotation.NONE) layerRotationMatches.get(r).add(entry.getKey());
                    }

                    continue;
                }
            }

            // Begin non-symmetric layer rotation match handling
            for (Rotation rotation : validRotations) {
                if (rotation == Rotation.NONE)
                    continue;

                IRecipeBlocks rotated = RecipeLayerUtil.rotate(layerBlocks, rotation);
                if (layer.matches(components, rotated)) {
                    layerRotationMatches.get(rotation).add(entry.getKey());
                }
            }
        }

        Optional<Rotation> firstMatched = layerRotationMatches.entrySet().stream()
                .filter((ent) -> ent.getValue().equals(layers.keySet()))
                .map(Map.Entry::getKey)
                .findFirst();

        return firstMatched.isPresent();
    }

    public ItemStackTemplate[] getOutputs() {
        return Stream.of(outputs).toArray(ItemStackTemplate[]::new);
    }

    public Map<String, Integer> getComponentTotals() {
        return this.cachedComponentTotals;
    }

    public int getComponentRequiredCount(String i) {
        return getComponentRequiredCount(i, this.components, this.layers);
    }

    private static int getComponentRequiredCount(String key, IRecipeComponents components, TreeMap<Integer, IRecipeLayer> layers) {
        if (!components.hasBlock(key))
            return 0;

        return layers.values().stream()
                .map(IRecipeLayer::getComponentTotals)
                .map(totals -> Optional.ofNullable(totals.get(key)).orElse(0))
                .mapToInt(Integer::intValue)
                .sum();
    }

    public AABB getDimensions() {
        return this.dimensions;
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if (y < layers.firstKey() || y > layers.lastKey())
            return Optional.empty();

        return Optional.ofNullable(this.layers.get(y));
    }

    public int getNumberLayers() {
        return layers.size();
    }

    @Override
    public Stream<IRecipeLayer> getLayers() {
        return layers.values().stream();
    }

    public MiniaturizationRecipeComponents getComponents() {
        return this.components;
    }

    public int getCraftingTime() {
        return this.requiredTime;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NonNull String group() {
        return "";
    }

    @Override
    public @NonNull RecipeSerializer<MiniaturizationRecipe> getSerializer() {
        return MiniaturizationRecipes.MINIATURIZATION_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<MiniaturizationRecipe> getType() {
        return MiniaturizationRecipes.MINIATURIZATION_RECIPE.get();
    }

    private List<IRecipeLayer> codecLayerList() {
        return layers.descendingKeySet().stream()
                .map(layers::get)
                .collect(Collectors.toList());
    }

    private List<ItemStackTemplate> codecOutputs() {
        return ImmutableList.copyOf(outputs);
    }

    private int codecRecipeSize() {
        if (this.hasFixedFootprint) return -1;
        // TODO: Change recipeSize to take an X/Z
        return (int) Math.max(dimensions.getXsize(), dimensions.getZsize());
    }
}
