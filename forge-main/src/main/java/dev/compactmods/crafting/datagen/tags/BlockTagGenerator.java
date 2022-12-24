package dev.compactmods.crafting.datagen.tags;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void addTags() {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        final var projector = CCBlocks.FIELD_PROJECTOR_BLOCK.get();
        pickaxe.add(projector);
        ironTool.add(projector);
    }
}
