package dev.compactmods.crafting.api.components;

import dev.compactmods.crafting.api.components.IRecipeComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IRecipeBlockComponent extends IRecipeComponent {

    boolean matches(BlockState state);

    Block getBlock();
    BlockState getRenderState();

    boolean didErrorRendering();
    void markRenderingErrored();
}
