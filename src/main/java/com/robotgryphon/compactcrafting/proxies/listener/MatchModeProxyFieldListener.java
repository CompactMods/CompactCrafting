package com.robotgryphon.compactcrafting.proxies.listener;

import com.robotgryphon.compactcrafting.proxies.block.FieldProxyBlock;
import dev.compactmods.compactcrafting.api.field.IFieldListener;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class MatchModeProxyFieldListener implements IFieldListener {

    private final World level;
    private final BlockPos location;

    public MatchModeProxyFieldListener(World level, BlockPos location) {
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
                    level.setBlock(location, updated, Constants.BlockFlags.DEFAULT_AND_RERENDER);
                }
            }
        }
    }
}
