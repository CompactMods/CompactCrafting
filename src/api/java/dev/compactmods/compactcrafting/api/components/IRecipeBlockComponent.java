package dev.compactmods.compactcrafting.api.components;

import net.minecraft.block.BlockState;

public interface IRecipeBlockComponent extends IRecipeComponent {
    boolean matches(BlockState state);

    BlockState getRenderState();

    boolean didErrorRendering();
    void markRenderingErrored();
}
