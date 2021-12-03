package dev.compactmods.crafting.client.fakeworld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class RenderingWorld extends Level {

    private final MiniaturizationRecipe recipe;

    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager = new RecipeManager();
    private final RenderingChunkProvider chunkProvider;

    public RenderingWorld(MiniaturizationRecipe recipe) {
        super(new RenderingSpawnInfo(), Level.OVERWORLD, DimensionType.DEFAULT_OVERWORLD,
                () -> InactiveProfiler.INSTANCE, true, false, 0);
        this.recipe = recipe;
        this.chunkProvider = new RenderingChunkProvider(this, recipe);
    }

    @Override
    public void sendBlockUpdated(BlockPos p_184138_1_, BlockState p_184138_2_, BlockState p_184138_3_, int p_184138_4_) {

    }

    @Override
    public void playSound(@Nullable Player p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundSource p_184148_9_, float p_184148_10_, float p_184148_11_) {

    }

    @Override
    public void playSound(@Nullable Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity(int p_73045_1_) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String p_217406_1_) {
        return null;
    }

    @Override
    public void setMapData(String p_151533_, MapItemSavedData p_151534_) {

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
    public TagContainer getTagManager() {
        return TagContainer.EMPTY;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkProvider;
    }

    @Override
    public void levelEvent(@Nullable Player p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

    }

    @Override
    public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

    }

    @Override
    public RegistryAccess registryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }

    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return Collections.emptyList();
    }

    @Override
    public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.THE_VOID);
    }
}
