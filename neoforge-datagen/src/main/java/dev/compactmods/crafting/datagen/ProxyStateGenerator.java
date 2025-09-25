package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ProxyStateGenerator extends BlockStateProvider {

    public ProxyStateGenerator(PackOutput pack, ExistingFileHelper files) {
        super(pack, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        getVariantBuilder(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        SharedStateGenerator.addProjectorBase(models().getBuilder("match_proxy"));
        SharedStateGenerator.addProjectorBase(models().getBuilder("rescan_proxy"));

        itemModels()
                .withExistingParent("match_proxy", modLoc("block/match_proxy"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();

        itemModels()
                .withExistingParent("rescan_proxy", modLoc("block/rescan_proxy"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();
    }
}
