package dev.compactmods.crafting.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;

public class RenderingSpawnInfo implements WritableLevelData {
    private static final GameRules RULES = new GameRules();

    private BlockPos spawnPos = BlockPos.ZERO;
    private float spawnAngle;

    @Override
    public void setSpawn(BlockPos pos, float angle) {
        this.spawnPos = pos;
        this.spawnAngle = angle;
    }

    @Override
    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    @Override
    public float getSpawnAngle() {
        return spawnAngle;
    }

    @Override
    public long getGameTime()
    {
        return 0;
    }

    @Override
    public long getDayTime()
    {
        return 0;
    }

    @Override
    public boolean isThundering()
    {
        return false;
    }

    @Override
    public boolean isRaining()
    {
        return false;
    }

    @Override
    public void setRaining(boolean isRaining)
    {

    }

    @Override
    public boolean isHardcore()
    {
        return false;
    }

    @Override
    public GameRules getGameRules() {
        return RULES;
    }

    @Override
    public Difficulty getDifficulty()
    {
        return Difficulty.PEACEFUL;
    }

    @Override
    public boolean isDifficultyLocked()
    {
        return false;
    }
}
