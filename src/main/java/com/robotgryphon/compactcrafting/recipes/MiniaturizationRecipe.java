package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.api.components.IRecipeBlockComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponents;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.api.layers.dim.IDynamicSizedRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.dim.IFixedSizedRecipeLayer;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.recipes.components.CCMiniRecipeComponents;
import com.robotgryphon.compactcrafting.recipes.components.EmptyBlockComponent;
import com.robotgryphon.compactcrafting.recipes.exceptions.MiniaturizationRecipeException;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerBlocks;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerUtil;
import com.robotgryphon.compactcrafting.recipes.setup.RecipeBase;
import com.robotgryphon.compactcrafting.server.ServerConfig;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends RecipeBase {

    /**
     * Only used for recipe dimension calculation from loading phase.
     * Specifies the minimum field size required for fluid recipe layers.
     */
    private final int recipeSize;
    private ResourceLocation id;
    private Map<Integer, IRecipeLayer> layers;
    private final ItemStack catalyst;
    private final ItemStack[] outputs;
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
        this.components = new CCMiniRecipeComponents();
    }

    public MiniaturizationRecipe(List<IRecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs,
                                 Map<String, IRecipeComponent> compMap) {
        this.recipeSize = -1;
        this.catalyst = catalyst;
        this.outputs = outputs.toArray(new ItemStack[0]);
        this.components = new CCMiniRecipeComponents();

        applyLayers(layers);
        applyComponents(compMap);
    }

    public MiniaturizationRecipe(int recipeSize, List<IRecipeLayer> layers,
                                 ItemStack catalyst, List<ItemStack> outputs,
                                 Map<String, IRecipeComponent> compMap) {
        this.recipeSize = recipeSize;
        this.catalyst = catalyst;
        this.outputs = outputs.toArray(new ItemStack[0]);
        this.components = new CCMiniRecipeComponents();

        applyLayers(layers);
        applyComponents(compMap);
    }

    void applyComponents(Map<String, IRecipeComponent> compMap) {
        this.cachedComponentTotals = new HashMap<>();

        components.clear();
        for (Map.Entry<String, IRecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if (comp.getValue() instanceof IRecipeBlockComponent) {
                components.registerBlock(comp.getKey(), (IRecipeBlockComponent) comp.getValue());
                continue;
            }

            components.registerOther(comp.getKey(), comp.getValue());
        }

        // Loop through layers, remap unknown components and warn
        for (IRecipeLayer layer : this.layers.values()) {
            Set<String> layerComponents = layer.getComponents();

            // Skip empty/malformed layer component requirements
            if (layerComponents == null || layerComponents.isEmpty())
                continue;

            for (String comp : layerComponents) {
                if (!components.hasBlock(comp)) {
                    CompactCrafting.LOGGER.warn(
                            "Warning: Unmapped component found in recipe; component '{}' being remapped to an empty block component.",
                            comp);

                    components.registerBlock(comp, new EmptyBlockComponent());
                }
            }
        }
    }

    private void applyLayers(List<IRecipeLayer> layers) {
        this.layers = new HashMap<>();
        ArrayList<IRecipeLayer> rev = new ArrayList<>(layers);
        Collections.reverse(rev);
        for (int y = 0; y < rev.size(); y++)
            this.layers.put(y, rev.get(y));
    }

    List<IRecipeLayer> getLayerListForCodecWrite() {
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
            if(!FieldProjectionSize.canFitDimensions(recipeSize)) {
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
    public boolean fitsInFieldSize(FieldProjectionSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXsize(), dimensions.getYsize(), dimensions.getZsize())
                .allMatch(size -> size <= dim);

        return fits;
    }

    public boolean matches(IWorldReader world, MiniaturizationField field) {
        if (!fitsInFieldSize(field.getFieldSize())) {
            if (ServerConfig.RECIPE_MATCHING.get())
                CompactCrafting.LOGGER.debug("Failing recipe {} for being too large to fit in field.", this.id);
            return false;
        }

        // We know that the recipe will at least fit inside the current projection field
        AxisAlignedBB filledBounds = field.getFilledBounds(world);

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

    private boolean checkRotation(IWorldReader world, Rotation rot, AxisAlignedBB filledBounds) {
        // Check the recipe layer by layer

        int maxY = (int) dimensions.getYsize();
        for (int offset = 0; offset < maxY; offset++) {
            Optional<IRecipeLayer> layer = getLayer(offset);

            // If we have no layer definition do lighter processing
            // TODO: Consider changing the layers to a map so we can make air layers null/nonexistent
            if (!layer.isPresent()) {
                // We're being safe here - if there's no layer definition we assume the layer is all-air
                return false;
            }

            AxisAlignedBB bounds = BlockSpaceUtil.getLayerBoundsByYOffset(filledBounds, offset);
            IRecipeLayerBlocks blocks = RecipeLayerBlocks.create(world, this, bounds);

            if (rot != Rotation.NONE)
                blocks = RecipeLayerUtil.rotate(blocks, rot);

            IRecipeLayer targetLayer = layer.get();

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
        return outputs;
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

    public Optional<String> getRecipeComponentKey(BlockState state) {
        // TODO - This might conflict with multiple matching states, consider handling this in the codec loading process
        for (String comp : this.components.getBlockComponents().keySet()) {
            boolean matched = this.components.getBlock(comp)
                    .map(c -> c.matches(state))
                    .orElse(false);

            if (matched)
                return Optional.of(comp);
        }

        return Optional.empty();
    }

    public boolean fitsInDimensions(AxisAlignedBB bounds) {
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

    public IRecipeComponents getComponents() {
        return this.components;
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

    public int getTicks() {
        return 200;
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
    public ResourceLocation getId() {
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
        return FieldProjectionSize.canFitDimensions(this.recipeSize);
    }

    public int getSize() {
        return this.recipeSize;
    }
}
