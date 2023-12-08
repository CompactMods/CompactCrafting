package dev.compactmods.crafting.client.fakeworld;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeChunk extends EmptyLevelChunk {
    private final MiniaturizationRecipe recipe;
    private final Map<BlockPos, BlockState> blockCache;
    private final Map<BlockPos, BlockEntity> tileCache;

    public RecipeChunk(RenderingWorld renderingLevel, ChunkPos chunkPos, MiniaturizationRecipe recipe) {
        super(renderingLevel, chunkPos, ForgeRegistries.BIOMES.getHolder(Biomes.THE_VOID).get());
        this.recipe = recipe;

        this.blockCache = new HashMap<>();
        this.tileCache = new HashMap<>();

        BlockSpaceUtil.getBlocksIn(recipe.getDimensions()).forEach(pos -> {
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

            if(posState.getBlock() instanceof EntityBlock eb) {
                BlockEntity tile = eb.newBlockEntity(pos.immutable(), posState);
                if(tile != null) {
                    tile.setLevel(renderingLevel);
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
    public BlockEntity getBlockEntity(BlockPos pos) {
        return tileCache.get(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos, EntityCreationType createType) {
        return tileCache.get(pos);
    }
}
