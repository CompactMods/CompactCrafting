package dev.compactmods.crafting.client.fakeworld;

import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class RenderingWorld extends World {

    private final MiniaturizationRecipe recipe;

    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager = new RecipeManager();
    private final RenderingChunkProvider chunkProvider;

    public RenderingWorld(MiniaturizationRecipe recipe) {
        super(new RenderingSpawnInfo(), World.OVERWORLD, DimensionType.DEFAULT_OVERWORLD,
                () -> EmptyProfiler.INSTANCE, true, false, 0);
        this.recipe = recipe;
        this.chunkProvider = new RenderingChunkProvider(this, recipe);
    }

    @Override
    public void sendBlockUpdated(BlockPos p_184138_1_, BlockState p_184138_2_, BlockState p_184138_3_, int p_184138_4_) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {

    }

    @Nullable
    @Override
    public Entity getEntity(int p_73045_1_) {
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(String p_217406_1_) {
        return null;
    }

    @Override
    public void setMapData(MapData p_217399_1_) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int p_175715_1_, BlockPos p_175715_2_, int p_175715_3_) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    @Override
    public ITagCollectionSupplier getTagManager() {
        return ITagCollectionSupplier.EMPTY;
    }

    @Override
    public ITickList<Block> getBlockTicks() {
        return null;
    }

    @Override
    public ITickList<Fluid> getLiquidTicks() {
        return EmptyTickList.empty();
    }

    @Override
    public AbstractChunkProvider getChunkSource() {
        return chunkProvider;
    }

    @Override
    public void levelEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

    }

    @Override
    public DynamicRegistries registryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }

    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> players() {
        return Collections.emptyList();
    }

    @Override
    public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.THE_VOID);
    }
}
