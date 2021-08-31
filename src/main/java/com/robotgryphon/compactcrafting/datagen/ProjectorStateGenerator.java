package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ProjectorStateGenerator extends BlockStateProvider {

    public ProjectorStateGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        projectorDishModel();
        projectorStaticModel();

        this.getVariantBuilder(Registration.FIELD_PROJECTOR_BLOCK.get())
                .forAllStates(state -> {
                    Direction dir = state.getValue(FieldProjectorBlock.FACING);
                    boolean active = FieldProjectorBlock.isActive(state);

                    if(active) {
                        return ConfiguredModel.builder()
                                .modelFile(models().getExistingFile(modLoc("block/base")))
                                .build();
                    } else {
                        return ConfiguredModel.builder()
                                .modelFile(models().getExistingFile(modLoc("block/field_projector_static")))
                                .rotationY(((int) dir.toYRot() - 90) % 360)
                                .build();
                    }
                });

        itemModels()
                .withExistingParent("projector_dish", modLoc("block/field_projector_dish"))
                .transforms()
                .transform(ModelBuilder.Perspective.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(2, -2, 0)
                .scale(1f, 1f, 1f)
                .end();

        itemModels()
                .withExistingParent("field_projector", modLoc("block/field_projector_static"))
                .transforms()
                .transform(ModelBuilder.Perspective.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();
    }

    private void projectorStaticModel() {
        BlockModelBuilder builder = models().getBuilder("block/field_projector_static")
                .texture("particle", modLoc("block/projector_base_bottom"));

        SharedStateGenerator.addProjectorBase(builder);
        addDishModel(builder);
    }

    private void projectorDishModel() {
        /**
         * WEST = FRONT
         * EAST = BACK
         * NORTH = LEFT SIDE
         * SOUTH = EAST SIDE
         */
        BlockModelBuilder builder = models().getBuilder("block/field_projector_dish")
                .texture("particle", modLoc("block/projector_dish_back"));

        // Dish
        addDishModel(builder);
    }

    private void addDishModel(BlockModelBuilder builder) {
        builder
                .texture("dish_front", modLoc("block/projector_dish_front"))
                .texture("dish_front_sides", modLoc("block/projector_dish_front_sides"))
                .texture("dish_back", modLoc("block/projector_dish_back"))
                .texture("dish_connector", modLoc("block/projector_dish_connector"));

        builder.element()
                .from(4, 8, 3)
                .to(6, 16, 13)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case UP:
                            face.texture("#dish_front_sides").uvs(1, 0, 11, 1).end();
                            break;

                        case DOWN:
                            face.texture("#dish_front_sides").uvs(1, 9, 11, 10).end();
                            break;

                        case EAST:
                            face.texture("#dish_back").uvs(0, 0, 10, 8).end();
                            break;

                        case WEST:
                            face.texture("#dish_front_sides").uvs(1, 1, 11, 9).end();
                            break;

                        case NORTH:
                            face.texture("#dish_front_sides").uvs(0, 1, 1, 9).end();
                            break;

                        case SOUTH:
                            face.texture("#dish_front_sides").uvs(11, 1, 12, 9).end();
                            break;
                    }
                })
                .shade(true)
                .end();

        // Front texture
        builder.element()
                .from(4, 8, 3)
                .to(5, 16, 13)
                .allFaces((dir, face) -> {
                    if(dir == Direction.WEST) {
                        face.texture("#dish_front").uvs(0, 0, 10, 8).tintindex(0).end();
                    } else {
                        face.texture("#dish_front").uvs(0, 0, 1, 1)
                                .cullface(dir.getOpposite()).end();
                    }
                })
                .end();

        builder.element()
                .from(6, 11, 7)
                .to(7, 13, 9)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case UP:
                            face.texture("#dish_connector").uvs(1, 0, 3, 1).end();
                            break;

                        case DOWN:
                            face.texture("#dish_connector").uvs(1, 3, 3, 4).end();
                            break;

                        case EAST:
                            // back
                            face.texture("#dish_connector").uvs(1, 1, 3, 3).end();
                            break;

                        case NORTH:
                            face.texture("#dish_connector").uvs(0, 1, 1, 3).end();
                            break;

                        case SOUTH:
                            face.texture("#dish_connector").uvs(3, 1, 4, 3).end();
                            break;
                    }
                })
                .end();
    }
}
