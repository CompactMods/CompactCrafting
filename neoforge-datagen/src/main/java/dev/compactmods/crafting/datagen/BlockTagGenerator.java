package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CompactCrafting.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(CCBlocks.FIELD_PROJECTOR_BLOCK.get())
                .add(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get())
                .add(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(CCBlocks.FIELD_PROJECTOR_BLOCK.get())
                .add(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get())
                .add(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());
    }
}
