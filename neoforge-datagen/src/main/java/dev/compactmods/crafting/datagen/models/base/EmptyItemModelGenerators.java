package dev.compactmods.crafting.datagen.models.base;

import net.minecraft.client.data.models.ItemModelGenerators;

public class EmptyItemModelGenerators extends ItemModelGenerators {
    public EmptyItemModelGenerators(ItemModelGenerators base) {
        super(base.itemModelOutput, base.modelOutput);
    }

    @Override
    public void run() {

    }
}
