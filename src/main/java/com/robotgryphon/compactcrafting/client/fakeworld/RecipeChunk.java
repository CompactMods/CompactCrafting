package com.robotgryphon.compactcrafting.client.fakeworld;

import dev.compactmods.compactcrafting.api.components.IRecipeBlockComponent;
import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.EmptyChunk;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeChunk extends EmptyChunk {
    private final MiniaturizationRecipe recipe;
    private final Map<BlockPos, BlockState> blockCache;
    private final Map<BlockPos, TileEntity> tileCache;

    public RecipeChunk(RenderingWorld renderingLevel, ChunkPos chunkPos, MiniaturizationRecipe recipe) {
        super(renderingLevel, chunkPos);
        this.recipe = recipe;

        this.blockCache = new HashMap<>();
        this.tileCache = new HashMap<>();

        recipe.getRelativeBlockPositions().forEach(pos -> {
            int y = pos.getY();
            Optional<IRecipeLayer> layer = recipe.getLayer(y);

            if(!layer.isPresent())
                return;

            IRecipeLayer rLayer = layer.get();
            Optional<String> componentForPosition = rLayer.getComponentForPosition(pos.below(y));

            BlockState posState = componentForPosition
                    .flatMap(recipe.getComponents()::getBlock)
                    .map(IRecipeBlockComponent::getRenderState)
                    .orElse(Blocks.VOID_AIR.defaultBlockState());

            blockCache.put(pos, posState);

            if(posState.hasTileEntity()) {
                TileEntity tile = posState.createTileEntity(renderingLevel);
                if(tile != null) {
                    tile.setLevelAndPosition(renderingLevel, pos.immutable());
                    tileCache.put(pos.immutable(), tile);
                }
            }
        });
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if(blockCache.containsKey(pos))
            return blockCache.get(pos);

        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPos pos) {
        return tileCache.get(pos);
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPos pos, CreateEntityType createType) {
        return tileCache.get(pos);
    }
}
