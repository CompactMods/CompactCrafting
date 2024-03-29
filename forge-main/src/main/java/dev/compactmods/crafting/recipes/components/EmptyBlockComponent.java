package dev.compactmods.crafting.recipes.components;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

public class EmptyBlockComponent implements IRecipeComponent, IRecipeBlockComponent {

    public static final Codec<EmptyBlockComponent> CODEC = Codec.unit(EmptyBlockComponent::new);

    @Override
    @SuppressWarnings("deprecated")
    public boolean matches(BlockState state) {
        // Update this when undeprecated; other modders -
        // if you aren't overriding the state properties, shame on you
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
        return ComponentRegistration.EMPTY_BLOCK_COMPONENT.get();
    }
}
