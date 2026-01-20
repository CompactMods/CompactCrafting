package dev.compactmods.crafting.datagen.models.base;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class EmptyBlockModelGenerators extends BlockModelGenerators {

    public EmptyBlockModelGenerators(BlockModelGenerators base) {
        super(base.blockStateOutput, base.itemModelOutput, base.modelOutput);
    }

    @Override
    public void run() {
    }

    public Identifier createSimpleWithTextures(Holder<Block> block, ModelTemplate template, TextureMapping mapping) {
        final var b = block.value();
        final var id = template.create(b, mapping, this.modelOutput);
        this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(b, BlockModelGenerators.plainVariant(id)));
        return id;
    }
}
