package dev.compactmods.crafting.recipes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.ISymmetricalLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.recipes.setup.RecipeBase;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class MiniaturizationRecipe extends RecipeBase implements IMiniaturizationRecipe {

    /**
     * Only used for recipe dimension calculation from loading phase.
     * Specifies the minimum field size required for fluid recipe layers.
     */
    private int recipeSize;
    private ResourceLocation id;
    private TreeMap<Integer, IRecipeLayer> layers;
    private ItemStack catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;

    private Map<String, Integer> cachedComponentTotals;
    private final IRecipeComponents components;

    public static final MiniaturizationRecipeCodec CODEC = new MiniaturizationRecipeCodec();

    public MiniaturizationRecipe() {
        this.recipeSize = -1;
        this.layers = new TreeMap<>();
        this.catalyst = ItemStack.EMPTY;
        this.outputs = new ItemStack[0];
        this.dimensions = AxisAlignedBB.ofSize(0, 0, 0);
        this.components = new MiniaturizationRecipeComponents();
    }

    void applyComponents(Map<String, IRecipeComponent> compMap) {
        components.clear();
        for (Map.Entry<String, IRecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if (comp.getValue() instanceof IRecipeBlockComponent) {
                components.registerBlock(comp.getKey(), (IRecipeBlockComponent) comp.getValue());
            }
        }

        layers.forEach((i, l) -> {
            // Allow the layer to drop components it deems non-required for matching (ie empty)
            l.dropNonRequiredComponents(components);

            // Remap any remaining required components as an empty component
            final Set<String> missing = l.getComponents()
                    .stream()
                    .filter(key -> !components.hasBlock(key))
                    .collect(Collectors.toSet());

            missing.forEach(needed -> components.registerBlock(needed, new EmptyBlockComponent()));
        });
    }

    public void applyLayers(List<IRecipeLayer> layers) {
        this.layers = new TreeMap<>();
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));
    }

    List<IRecipeLayer> getLayerListForCodecWrite() {
        return layers.descendingKeySet().stream()
                .map(layers::get)
                .collect(Collectors.toList());
    }

    void recalculateDimensions() {
        int height = this.layers.size();
        int x = 0;
        int z = 0;

        boolean hasAnyRigidLayers = this.layers.values().stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        if (!hasAnyRigidLayers) {
            this.dimensions = new AxisAlignedBB(0, 0, 0, recipeSize, height, recipeSize);
        } else {
            for (IRecipeLayer l : this.layers.values()) {
                // We only need to worry about fixed-dimension layers; the fluid layers will adapt
                if (l instanceof IFixedSizedRecipeLayer) {
                    AxisAlignedBB dimensions = ((IFixedSizedRecipeLayer) l).getDimensions();
                    if (dimensions.getXsize() > x)
                        x = (int) Math.ceil(dimensions.getXsize());

                    if (dimensions.getZsize() > z)
                        z = (int) Math.ceil(dimensions.getZsize());
                }
            }

            this.dimensions = new AxisAlignedBB(Vector3d.ZERO, new Vector3d(x, height, z));
        }

        updateFluidLayerDimensions();
    }

    private void updateFluidLayerDimensions() {
        // Update all the dynamic recipe layers
        final AxisAlignedBB footprint = BlockSpaceUtil.getLayerBounds(dimensions, 0);
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
        AxisAlignedBB filledBounds = blocks.getFilledBounds();

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

    public AxisAlignedBB getDimensions() {
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

    public IRecipeComponents getComponents() {
        return this.components;
    }

    @Override
    public void setOutputs(Collection<ItemStack> outputs) {
        this.outputs = outputs.toArray(new ItemStack[0]);
    }

    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    public int getCraftingTime() {
        return 200;
    }


    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registration.MINIATURIZATION_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registration.MINIATURIZATION_RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getRecipeIdentifier() {
        return this.id;
    }

    @Override
    public void setId(ResourceLocation recipeId) {
        this.id = recipeId;
    }

    public boolean hasSpecifiedSize() {
        return MiniaturizationFieldSize.canFitDimensions(this.recipeSize);
    }

    public int getRecipeSize() {
        return this.recipeSize;
    }

    public void setRecipeSize(int size) {
        if (!MiniaturizationFieldSize.canFitDimensions(size))
            return;

        this.recipeSize = size;
        this.recalculateDimensions();
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst.copy();
    }
}
