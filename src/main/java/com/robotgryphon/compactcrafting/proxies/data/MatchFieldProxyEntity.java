package com.robotgryphon.compactcrafting.proxies.data;

import com.robotgryphon.compactcrafting.Registration;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import com.robotgryphon.compactcrafting.proxies.block.FieldProxyBlock;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.util.Constants;

public class MatchFieldProxyEntity extends BaseFieldProxyEntity {
    public MatchFieldProxyEntity() {
        super(Registration.MATCH_PROXY_ENTITY.get());
    }

    @Override
    public void recipeChanged(IMiniaturizationField field, MiniaturizationRecipe recipe) {
        super.recipeChanged(field, recipe);

        if(level != null && !level.isClientSide) {
            boolean hasRecipe = recipe != null;

            int newPower = hasRecipe ? 15 : 0;

            BlockState current = this.getBlockState();
            if(current.getValue(FieldProxyBlock.SIGNAL) != newPower) {
                BlockState updated = current.setValue(FieldProxyBlock.SIGNAL, newPower);
                level.setBlock(worldPosition, updated, Constants.BlockFlags.DEFAULT_AND_RERENDER);
            }
        }
    }
}
