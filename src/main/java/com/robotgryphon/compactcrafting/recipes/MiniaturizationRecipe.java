package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import jdk.nashorn.internal.ir.BlockStatement;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    protected void recalculateDimensions() {
        this.dimensions = new AxisAlignedBB(BlockPos.ZERO);
    }

    public boolean addComponent(String key, BlockState block) {
        if(components.containsKey(key))
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
    private boolean fitsInFieldSize(FieldProjectionSize fieldSize) {
        int dim = fieldSize.getDimensions();
        boolean fits = Stream.of(dimensions.getXSize(), dimensions.getYSize(), dimensions.getZSize())
                .allMatch(size -> size <= dim);

        return fits;
    }

    public boolean matches(IWorldReader world, FieldProjectionSize fieldSize, AxisAlignedBB field) {
        if (!fitsInFieldSize(fieldSize))
            return false;

        return true;
    }

    public ItemStack[] getOutputs() {
        return outputs;
    }
}
