package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.projector.FieldProjectorTags;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, CompactCrafting.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FieldProjectorTags.PROJECTOR_BLOCK)
                .addTag(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK)
                .addTag(FieldProjectorTags.INACTIVE_PROJECTOR_BLOCK);

        tag(FieldProjectorTags.INACTIVE_PROJECTOR_BLOCK)
                .add(CCBlocks.INACTIVE_FIELD_PROJECTOR_BLOCK.get());

        tag(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK)
                .add(CCBlocks.FIELD_PROJECTOR_BLOCK.get());
    }
}