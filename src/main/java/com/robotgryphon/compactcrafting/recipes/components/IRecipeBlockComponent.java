package com.robotgryphon.compactcrafting.recipes.components;

import net.minecraft.block.BlockState;

public interface IRecipeBlockComponent extends IRecipeComponent {
    boolean matches(BlockState state);

    BlockState getRenderState();
}
