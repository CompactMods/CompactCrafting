package dev.compactmods.crafting.api.components;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public interface IRecipeBlockComponent extends IRecipeComponent {

    boolean matches(BlockState state);

    Block getBlock();
    BlockState getRenderState();

    boolean didErrorRendering();
    void markRenderingErrored();
}
