package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class MiniaturizationRecipe extends ForgeRegistryEntry<MiniaturizationRecipe> {

    public IRecipeLayer[] layers;
    public Item catalyst;
    private ItemStack[] outputs;
    private AxisAlignedBB dimensions;

    public MiniaturizationRecipe() {
        this.layers = new IRecipeLayer[0];
        this.outputs = new ItemStack[0];
        this.dimensions = AxisAlignedBB.withSizeAtOrigin(0, 0, 0);
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
