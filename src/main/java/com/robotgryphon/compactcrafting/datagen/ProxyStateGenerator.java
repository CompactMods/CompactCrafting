package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ProxyStateGenerator extends BlockStateProvider {

    public ProxyStateGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(Registration.MATCH_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        getVariantBuilder(Registration.RESCAN_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        SharedStateGenerator.addProjectorBase(models().getBuilder("match_proxy"));
        SharedStateGenerator.addProjectorBase(models().getBuilder("rescan_proxy"));

        itemModels()
                .withExistingParent("match_proxy", modLoc("block/match_proxy"))
                .transforms()
                .transform(ModelBuilder.Perspective.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();

        itemModels()
                .withExistingParent("rescan_proxy", modLoc("block/rescan_proxy"))
                .transforms()
                .transform(ModelBuilder.Perspective.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();
    }
}
