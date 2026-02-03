package dev.compactmods.crafting.recipes.components;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EmptyBlockComponent implements IRecipeComponent, IRecipeBlockComponent {

    public static final MapCodec<EmptyBlockComponent> CODEC = MapCodec.unit(EmptyBlockComponent::new);

    @Override
    public boolean matches(BlockState state) {
        return state.isAir();
    }

    @Override
    public Block getBlock() {
        return Blocks.AIR;
    }

    @Override
    public BlockState getRenderState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean didErrorRendering() {
        return false;
    }

    @Override
    public void markRenderingErrored() {
    }

    @Override
    public RecipeComponentType<?> getType() {
        return RecipeComponents.EMPTY_BLOCK_COMPONENT.get();
    }
}
