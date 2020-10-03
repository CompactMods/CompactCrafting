package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.crafting.CraftingHelper;
import com.robotgryphon.compactcrafting.field.FieldHelper;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends ForgeRegistryEntry<MiniaturizationRecipe> {

    public IRecipeLayer[] layers;
    public Item catalyst;
    public ItemStack[] outputs;
    private AxisAlignedBB dimensions;

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private Map<String, BlockState> components;

    public MiniaturizationRecipe() {
        this.layers = new IRecipeLayer[0];
        this.outputs = new ItemStack[0];
        this.components = new HashMap<>();

        recalculateDimensions();
    }

    public void recalculateDimensions() {
        this.dimensions = new AxisAlignedBB(BlockPos.ZERO);
    }

    public boolean addComponent(String key, BlockState block) {
        if (components.containsKey(key))
            return false;

        components.put(key, block);
        return true;
    }

    /**
     * Checks that a given field size can contain this recipe.
     *
     * @param fieldSize
     * @return
     */
    public boolean fitsInFieldSize(FieldProjectionSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXSize(), dimensions.getYSize(), dimensions.getZSize())
                .allMatch(size -> size <= dim);

        return fits;
    }

    private boolean hasMatchingBottomLayer(IWorldReader world, FieldProjectionSize fieldSize, AxisAlignedBB field) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        return true;
    }

    public boolean matches(IWorldReader world, FieldProjectionSize fieldSize, AxisAlignedBB field) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        Stream<AxisAlignedBB> fieldLayers = FieldHelper.splitIntoLayers(fieldSize, field);
        double recipeHeight = dimensions.getYSize();
        int maxRecipeBottomLayer = (int) Math.floor(field.maxY - recipeHeight);

        // A list of all the possible layers of the field that COULD match the recipe
        List<AxisAlignedBB> possibleRecipeBottomLayers = fieldLayers
                .filter(layer -> layer.minY <= maxRecipeBottomLayer)

                // TODO: Allow "air" layers at bottom of recipe
                .filter(layer -> CraftingHelper.hasBlocksInField(world, layer))
                .collect(Collectors.toList());

        // Check each layer bottom to see if it matches the bottom layer of this recipe
        return possibleRecipeBottomLayers.stream()
                .anyMatch(fieldLayer -> layers[0].matchesFieldLayer(world, this, fieldSize, fieldLayer));
    }

    public ItemStack[] getOutputs() {
        return outputs;
    }

    public Optional<BlockState> getRecipeComponent(String i) {
        if (this.components.containsKey(i)) {
            BlockState component = components.get(i);
            return Optional.of(component);
        }

        return Optional.empty();
    }

    public Optional<String> getRecipeComponentKey(BlockState state) {
        for(String comp : this.components.keySet()) {
            if(components.get(comp) == state)
                return Optional.of(comp);
        }

        return Optional.empty();
    }
}
