package com.robotgryphon.compactcrafting.recipes.layers.dim;

import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;

public interface IRigidRecipeLayer extends IRecipeLayer {
    AxisAlignedBB getDimensions();
}
