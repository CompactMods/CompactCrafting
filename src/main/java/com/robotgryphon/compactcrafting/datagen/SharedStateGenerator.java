package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SharedStateGenerator extends BlockStateProvider {

    public SharedStateGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        baseModel();

        itemModels()
                .withExistingParent("base", modLoc("block/base"))
                .transforms()
                .transform(ModelBuilder.Perspective.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();
    }

    private void baseModel() {
        BlockModelBuilder builder = models().getBuilder("block/base")
                .texture("particle", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_base_bottom"));

        addProjectorBase(builder);
    }

    static void addProjectorBase(BlockModelBuilder builder) {
        builder
                .texture("base_top", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_base_top"))
                .texture("base_top_cutout", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_base_top_cutout"))
                .texture("base_bottom", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_base_bottom"))
                .texture("base_side", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_base_side"))
                .texture("pole", new ResourceLocation(CompactCrafting.MOD_ID, "block/projector_pole"));

        // Base
        builder.element()
                .from(0, 0, 0)
                .to(16, 6, 16)
                .shade(true)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case NORTH:
                        case SOUTH:
                        case WEST:
                        case EAST:
                            face.texture("#base_side").uvs(0, 10, 16, 16).end();
                            break;

                        case UP:
                            face.texture("#base_top").uvs(0, 0, 16, 16).end();
                            break;

                        case DOWN:
                            face.texture("#base_bottom").uvs(0, 0, 16, 16).end();
                            break;
                    }
                })
                .end();

        builder.element()
                .from(0, 0, 0)
                .to(16, 6, 16)
                .shade(true)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case UP:
                            face.texture("#base_top_cutout").uvs(0, 0, 16, 16)
                                    .tintindex(1).end();
                            break;

                        default:
                            // transparent area of texture
                            face.texture("#base_top_cutout").uvs(0, 0, 1, 1)
                                    .cullface(dir.getOpposite()).end();
                            break;
                    }
                })
                .end();

        builder.element()
                .from(7, 6, 7)
                .to(9, 12, 9)
                .shade(true)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case NORTH:
                        case SOUTH:
                        case WEST:
                        case EAST:
                            face.texture("#pole").uvs(0, 2, 2, 10).end();
                            break;

                        case UP:
                            face.texture("#pole").uvs(0, 0, 2, 2).end();
                            break;
                    }
                })
                .end();
    }
}
