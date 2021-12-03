package dev.compactmods.crafting.proxies.listener;

import javax.annotation.Nullable;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MatchModeProxyFieldListener implements IFieldListener {

    private final Level level;
    private final BlockPos location;

    public MatchModeProxyFieldListener(Level level, BlockPos location) {
        this.level = level;
        this.location = location;
    }

    @Override
    public void onRecipeChanged(IMiniaturizationField field, @Nullable IMiniaturizationRecipe recipe) {
        if (level != null) {
            BlockState currentState = level.getBlockState(location);
            if (currentState.getBlock() instanceof FieldProxyBlock) {
                boolean hasRecipe = recipe != null;

                int newPower = hasRecipe ? 15 : 0;

                if (currentState.getValue(FieldProxyBlock.SIGNAL) != newPower) {
                    BlockState updated = currentState.setValue(FieldProxyBlock.SIGNAL, newPower);
                    level.setBlock(location, updated, Block.UPDATE_ALL);
                }
            }
        }
    }
}
