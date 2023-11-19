package dev.compactmods.crafting.datagen;

import org.jetbrains.annotations.Nullable;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGenerator extends BlockTagsProvider {
    
    public BlockTagGenerator(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, CompactCrafting.MOD_ID, existingFileHelper);
    }
    
    @Override
    protected void addTags() {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(CCBlocks.FIELD_PROJECTOR_BLOCK.get(),
                        CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(),
                        CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());
    }
}
