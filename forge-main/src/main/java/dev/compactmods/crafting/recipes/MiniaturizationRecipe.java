package dev.compactmods.crafting.recipes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IPositionalComponentLookup;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.ISymmetricalLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.catalyst.CatalystMatcherCodec;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.recipes.setup.RecipeBase;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import dev.compactmods.crafting.util.CodecExtensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MiniaturizationRecipe extends RecipeBase implements IMiniaturizationRecipe {

    /**
     * Only used for recipe dimension calculation from loading phase.
     * Specifies the minimum field size required for fluid recipe layers.
     */
//    private int recipeSize;
    private ResourceLocation id;
    private final TreeMap<Integer, IRecipeLayer> layers;
    private final ICatalystMatcher catalyst;
    private final ItemStack[] outputs;
    private final AABB dimensions;
    private final int requiredTime;
    private final boolean hasFixedFootprint;
    private Map<String, Integer> cachedComponentTotals;
    private final MiniaturizationRecipeComponents components;

    public static final Codec<MiniaturizationRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("craftingTime", 200)
                    .forGetter(MiniaturizationRecipe::getCraftingTime),

            Codec.INT.optionalFieldOf("recipeSize", -1)
                    .forGetter(MiniaturizationRecipe::codecRecipeSize),

            MiniaturizationRecipeCodec.LAYER_CODEC.listOf().fieldOf("layers")
                    .forGetter(MiniaturizationRecipe::getLayerListForCodecWrite),

            MiniaturizationRecipeComponents.CODEC.optionalFieldOf("components", MiniaturizationRecipeComponents.EMPTY)
                    .forGetter(MiniaturizationRecipe::getComponents),

            CodecExtensions.FRIENDLY_ITEMSTACK.listOf().fieldOf("outputs")
                    .forGetter(MiniaturizationRecipe::codecOutputs),

            CatalystMatcherCodec.MATCHER_CODEC.fieldOf("catalyst")
                    .forGetter(MiniaturizationRecipe::getCatalyst)

    ).apply(i, MiniaturizationRecipe::new));

    public MiniaturizationRecipe() {
        this.hasFixedFootprint = true;
        this.layers = new TreeMap<>();
        this.outputs = new ItemStack[0];
        this.catalyst = null;
        this.dimensions = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
        this.components = new MiniaturizationRecipeComponents();
        this.requiredTime = 200;
    }

    public MiniaturizationRecipe(int craftTime, int recipeSize, List<IRecipeLayer> layers,
                                 MiniaturizationRecipeComponents components, List<ItemStack> outputs,
                                 ICatalystMatcher catalyst) {
        this.layers = new TreeMap<>();
        this.outputs = outputs.toArray(new ItemStack[0]);
        this.catalyst = catalyst;
        this.components = components;
        this.requiredTime = craftTime;

        // region Layers
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));
        // endregion

        // region Missing components
        final var tempUnknownKeys = layers.stream()
                .map(IRecipeLayer::getComponents)
                .flatMap(Set::stream)
                .filter(key -> !components.isKnownKey(key))
                .collect(Collectors.toUnmodifiableSet());

        for (String key : tempUnknownKeys) {
            CompactCrafting.RECIPE_LOGGER.warn("Got layer-required component '{}' but it was not defined in the recipe; removing.", key);

            // Only supports fixed-size layers (particular example is mixed layers, which need to specify gaps)
            // Fluid dimension layers having unknown components is almost certainly a bug on the recipe author
            for (IRecipeLayer layer : layers) {
                if (layer instanceof IFixedSizedRecipeLayer fixedLayer) {
                    IPositionalComponentLookup p = fixedLayer.getComponentLookup();
                    p.remove(key);
                }
            }
        }
        // endregion

        // region Recalculate Dimensions
        int height = this.layers.size();
        int x = 0;
        int z = 0;

        this.hasFixedFootprint = this.layers.values().stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        if (!hasFixedFootprint) {
            if (recipeSize < 1) {
                CompactCrafting.RECIPE_LOGGER.warn("Warning: recipe dimensions are not strictly defined but recipeSize is not set. Forcing it to 1.");
                recipeSize = 1;
            }

            this.dimensions = new AABB(0, 0, 0, recipeSize, height, recipeSize);
        } else {
            for (IRecipeLayer l : this.layers.values()) {
                // We only need to worry about fixed-dimension layers; the fluid layers will adapt
                if (l instanceof IFixedSizedRecipeLayer) {
                    AABB dimensions = ((IFixedSizedRecipeLayer) l).getDimensions();
                    if (dimensions.getXsize() > x)
                        x = (int) Math.ceil(dimensions.getXsize());

                    if (dimensions.getZsize() > z)
                        z = (int) Math.ceil(dimensions.getZsize());
                }
            }

            this.dimensions = new AABB(Vec3.ZERO, new Vec3(x, height, z));
        }

        this.updateFluidLayerDimensions();
        // endregion
    }

    List<IRecipeLayer> getLayerListForCodecWrite() {
        return layers.descendingKeySet().stream()
                .map(layers::get)
                .collect(Collectors.toList());
    }

    private void updateFluidLayerDimensions() {
        // Update all the dynamic recipe layers
        final AABB footprint = BlockSpaceUtil.getLayerBounds(dimensions, 0);
        this.layers.values()
                .stream()
                .filter(l -> l instanceof IDynamicSizedRecipeLayer)
                .forEach(dl -> ((IDynamicSizedRecipeLayer) dl).setRecipeDimensions(footprint));
    }

    /**
     * Checks that a given field size can contain this recipe.
     *
     * @param fieldSize
     * @return
     */
    public boolean fitsInFieldSize(MiniaturizationFieldSize fieldSize) {
        int dim = fieldSize.getDimensions();
        return (dimensions.getXsize() <= dim) &&
                (dimensions.getYsize() <= dim) &&
                (dimensions.getZsize() <= dim);
    }

    public boolean matches(IRecipeBlocks blocks) {
        final boolean matchLogging = ServerConfig.RECIPE_MATCHING.get();

        if (!BlockSpaceUtil.boundsFitsInside(blocks.getFilledBounds(), dimensions)) {
            if (matchLogging)
                CompactCrafting.LOGGER.debug("Failing recipe {} for being too large to fit in field.", this.id);
            return false;
        }

        // We know that the recipe will at least fit inside the current projection field
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
                    if (matchLogging)
                        CompactCrafting.RECIPE_LOGGER.debug("[{}] Failing recipe layer {}; marked symmetrical and does not match its first rotation attempt.", this.id, entry.getKey());

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

    public ItemStack[] getOutputs() {
        return Stream.of(outputs)
                .map(ItemStack::copy)
                .toArray(ItemStack[]::new);
    }

    public Map<String, Integer> getComponentTotals() {
        if (this.cachedComponentTotals != null)
            return this.cachedComponentTotals;

        HashMap<String, Integer> totals = new HashMap<>();
        components.getAllComponents().keySet().forEach(comp -> {
            int count = this.getComponentRequiredCount(comp);
            totals.put(comp, count);
        });

        this.cachedComponentTotals = totals;
        return totals;
    }

    public int getComponentRequiredCount(String i) {
        if (!this.components.hasBlock(i))
            return 0;

        return layers.values().stream()
                .map(IRecipeLayer::getComponentTotals)
                .map(totals -> Optional.ofNullable(totals.get(i)).orElse(0))
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

    public ICatalystMatcher getCatalyst() {
        return this.catalyst;
    }

    public int getCraftingTime() {
        return this.requiredTime;
    }


    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCMiniaturizationRecipes.MINIATURIZATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CCMiniaturizationRecipes.MINIATURIZATION_RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getRecipeIdentifier() {
        return this.id;
    }

    @Override
    public void setId(ResourceLocation recipeId) {
        this.id = recipeId;
    }

    private List<ItemStack> codecOutputs() {
        return ImmutableList.copyOf(outputs);
    }

    private int codecRecipeSize() {
        if (this.hasFixedFootprint) return -1;
        // TODO: Change recipeSize to take an X/Z
        return (int) Math.max(dimensions.getXsize(), dimensions.getZsize());
    }
}
