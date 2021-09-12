package dev.compactmods.crafting.recipes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableList;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.exceptions.MiniaturizationRecipeException;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class MiniaturizationRecipe extends RecipeBase implements IMiniaturizationRecipe {

    /**
     * Only used for recipe dimension calculation from loading phase.
     * Specifies the minimum field size required for fluid recipe layers.
     */
    private int recipeSize;
    private ResourceLocation id;
    private Map<Integer, IRecipeLayer> layers;
    private final ItemStack catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;

    private Map<String, Integer> cachedComponentTotals;
    private IRecipeComponents components;

    public static final MiniaturizationRecipeCodec CODEC = new MiniaturizationRecipeCodec();

    public MiniaturizationRecipe() {
        this.recipeSize = -1;
        this.layers = new HashMap<>();
        this.catalyst = ItemStack.EMPTY;
        this.outputs = new ItemStack[0];
        this.dimensions = AxisAlignedBB.ofSize(0, 0, 0);
        this.components = new MiniaturizationRecipeComponents();
    }

    public MiniaturizationRecipe(List<IRecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs,
                                 Map<String, IRecipeComponent> compMap) {
        this.recipeSize = -1;
        this.catalyst = catalyst;
        this.outputs = outputs.toArray(new ItemStack[0]);
        this.components = new MiniaturizationRecipeComponents();

        applyLayers(layers);
        applyComponents(compMap);
    }

    public MiniaturizationRecipe(int recipeSize, List<IRecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs,
                                 Map<String, IRecipeComponent> compMap) {
        this.recipeSize = recipeSize;
        this.catalyst = catalyst;
        this.outputs = outputs.toArray(new ItemStack[0]);
        this.components = new MiniaturizationRecipeComponents();

        applyLayers(layers);
        applyComponents(compMap);
    }

    void applyComponents(Map<String, IRecipeComponent> compMap) {
        components.clear();
        for (Map.Entry<String, IRecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if (comp.getValue() instanceof IRecipeBlockComponent) {
                components.registerBlock(comp.getKey(), (IRecipeBlockComponent) comp.getValue());
                continue;
            }

            components.registerOther(comp.getKey(), comp.getValue());
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
        this.layers = new HashMap<>();
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));
    }

    List<IRecipeLayer> getLayerListForCodecWrite() {
        if(layers.isEmpty())
            return Collections.emptyList();

        ImmutableList.Builder<IRecipeLayer> l = ImmutableList.builder();
        for (int y = layers.size() - 1; y >= 0; y--)
            l.add(layers.get(y));

        return l.build();
    }

    void recalculateDimensions() {
        int height = this.layers.size();
        int x = 0;
        int z = 0;

        boolean hasAnyRigidLayers = this.layers.values().stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        if (!hasAnyRigidLayers) {
            if (!MiniaturizationFieldSize.canFitDimensions(recipeSize)) {
                CompactCrafting.LOGGER.error("Error: tried to enforce a dimension update with a recipe size that will not fit inside field boundaries.");
            } else {
                setFluidDimensions(new AxisAlignedBB(0, 0, 0, recipeSize, height, recipeSize));
            }
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
        this.layers.values()
                .stream()
                .filter(l -> l instanceof IDynamicSizedRecipeLayer)
                .forEach(dl -> ((IDynamicSizedRecipeLayer) dl).setRecipeDimensions(dimensions));
    }

    /**
     * Checks that a given field size can contain this recipe.
     *
     * @param fieldSize
     * @return
     */
    public boolean fitsInFieldSize(MiniaturizationFieldSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXsize(), dimensions.getYsize(), dimensions.getZsize())
                .allMatch(size -> size <= dim);

        return fits;
    }

    public boolean matches(IBlockReader world, MiniaturizationField field) {
        if (!fitsInFieldSize(field.getFieldSize())) {
            if (ServerConfig.RECIPE_MATCHING.get())
                CompactCrafting.LOGGER.debug("Failing recipe {} for being too large to fit in field.", this.id);
            return false;
        }

        // We know that the recipe will at least fit inside the current projection field
        AxisAlignedBB filledBounds = field.getFilledBounds();

        Rotation[] validRotations = new Rotation[]{
                Rotation.NONE,
                Rotation.CLOCKWISE_90,
                Rotation.CLOCKWISE_180,
                Rotation.COUNTERCLOCKWISE_90
        };

        for (Rotation rot : validRotations) {
            boolean matchesRot = checkRotation(world, rot, filledBounds);
            if (matchesRot)
                return true;
        }

        if (ServerConfig.RECIPE_MATCHING.get())
            CompactCrafting.RECIPE_LOGGER.debug("[{}] Failing recipe for not matching any rotations.", this.id);
        return false;
    }

    private boolean checkRotation(IBlockReader world, Rotation rot, AxisAlignedBB filledBounds) {
        // Check the recipe layer by layer

        int maxY = (int) dimensions.getYsize();
        for (int offset = 0; offset < maxY; offset++) {
            Optional<IRecipeLayer> layer = getLayer(offset);

            // If we have no layer definition do lighter processing
            if (!layer.isPresent()) {
                return false;
            }

            AxisAlignedBB bounds = BlockSpaceUtil.getLayerBounds(filledBounds, offset);
            IRecipeLayerBlocks blocks = RecipeLayerBlocks.create(world, this.components, bounds);

            if (rot != Rotation.NONE)
                blocks = RecipeLayerUtil.rotate(blocks, rot);

            IRecipeLayer targetLayer = layer.get();

            // If the layer spec requires all components to be known (by default) then check early
            if (targetLayer.requiresAllBlocksIdentified() && !blocks.allIdentified())
                return false;

            boolean layerMatched = targetLayer.matches(components, blocks);

            if (!layerMatched) {
                if (ServerConfig.RECIPE_MATCHING.get())
                    CompactCrafting.RECIPE_LOGGER.debug("[{}] Failing recipe at layer {} ({}) because it did not match rotation {}.", this.id, offset, targetLayer.getType().getRegistryName(), rot);

                return false;
            }
        }

        return true;
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

        int required = 0;
        for (IRecipeLayer layer : this.layers.values()) {
            if (layer == null)
                continue;

            Map<String, Integer> layerTotals = layer.getComponentTotals();

            if (layerTotals.containsKey(i))
                required += layerTotals.get(i);
        }

        return required;
    }

    public boolean fitsInDimensions(AxisAlignedBB bounds) {
        if (this.dimensions == null || bounds == null)
            return false;

        return BlockSpaceUtil.boundsFitsInside(this.dimensions, bounds);
    }

    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    public Optional<IRecipeLayer> getLayer(int y) {
        if (y < 0 || y > this.layers.size() - 1)
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

    public void setComponents(IRecipeComponents components) {
        this.components = components;
    }

    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    public void setFluidDimensions(AxisAlignedBB dimensions) {
        boolean hasRigidLayer = this.layers.values().stream().anyMatch(layer -> layer instanceof IFixedSizedRecipeLayer);
        if (!hasRigidLayer) {
            this.dimensions = dimensions;
            updateFluidLayerDimensions();
        } else {
            CompactCrafting.LOGGER.warn("Tried to set fluid dimensions when a rigid layer is present in the layer set.", new MiniaturizationRecipeException("no. bad."));
        }
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

    public Stream<BlockPos> getRelativeBlockPositions() {
        AxisAlignedBB realBounds = dimensions.contract(1, 1, 1);
        return BlockPos.betweenClosedStream(realBounds);
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
}
