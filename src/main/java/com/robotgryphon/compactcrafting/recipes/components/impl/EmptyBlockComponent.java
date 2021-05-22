package com.robotgryphon.compactcrafting.recipes.components.impl;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.robotgryphon.compactcrafting.recipes.components.ComponentRegistration;
import com.robotgryphon.compactcrafting.api.components.IRecipeBlockComponent;
import com.robotgryphon.compactcrafting.api.components.RecipeComponentType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class EmptyBlockComponent implements IRecipeBlockComponent {

    public static final Codec<Unit> CODEC = Codec.EMPTY.codec();

    @Override
    public boolean matches(BlockState state) {
        // Update this when undeprecated; other modders -
        // if you aren't overriding the state properties, shame on you
        return state.isAir();
    }

    @Override
    public BlockState getRenderState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public RecipeComponentType<?> getType() {
        return ComponentRegistration.EMPTY_BLOCK_COMPONENT.get();
    }
}
