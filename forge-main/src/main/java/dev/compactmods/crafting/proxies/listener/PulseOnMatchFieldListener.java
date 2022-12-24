package dev.compactmods.crafting.proxies.listener;

import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PulseOnMatchFieldListener implements IFieldListener {

    @Override
    public void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {
        final var level = field.level();
        final var location = field.getCenter();

        if (level != null) {
            BlockState currentState = level.getBlockState(location);
            if (currentState.getBlock() instanceof FieldProxyBlock) {
                BlockState updated = currentState.setValue(FieldProxyBlock.SIGNAL, 15);
                level.setBlock(location, updated, Block.UPDATE_ALL);
                level.scheduleTick(location, updated.getBlock(), 2);
            }
        }
    }
}
