package com.robotgryphon.compactcrafting.api.components;

import net.minecraft.block.BlockState;

public interface IRecipeBlockComponent extends IRecipeComponent {
    boolean matches(BlockState state);

    BlockState getRenderState();
}
