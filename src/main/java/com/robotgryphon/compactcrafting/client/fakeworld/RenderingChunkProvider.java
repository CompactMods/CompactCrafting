package com.robotgryphon.compactcrafting.client.fakeworld;

import com.mojang.datafixers.util.Pair;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenderingChunkProvider extends AbstractChunkProvider {
    private final MiniaturizationRecipe recipe;

    private final Map<ChunkPos, IChunk> chunks;
    private final RenderingWorld renderingLevel;
    private final WorldLightManager lightManager;

    public RenderingChunkProvider(RenderingWorld renderingLevel, MiniaturizationRecipe recipe) {
        this.recipe = recipe;

        this.renderingLevel = renderingLevel;
        this.lightManager = new WorldLightManager(this, true, true);

        Map<ChunkPos, List<BlockPos>> byChunk = new HashMap<>();
        BlockPos.betweenClosedStream(this.recipe.getDimensions())
                .map(BlockPos::immutable)
                .forEach(pos -> {
                    byChunk.computeIfAbsent(new ChunkPos(pos), $ -> new ArrayList<>()).add(pos);
                });

        chunks = byChunk.keySet().stream()
                .map(chunkPos -> Pair.of(chunkPos, new RecipeChunk(this.renderingLevel, chunkPos, recipe)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    @Nullable
    @Override
    public IChunk getChunk(int cx, int cz, ChunkStatus status, boolean load) {
        return chunks.computeIfAbsent(new ChunkPos(cx, cz), p -> new EmptyChunk(renderingLevel, p));
    }

    @Override
    public String gatherStats() {
        return "?";
    }

    @Override
    public WorldLightManager getLightEngine() {
        return lightManager;
    }

    @Override
    public IBlockReader getLevel() {
        return renderingLevel;
    }
}
