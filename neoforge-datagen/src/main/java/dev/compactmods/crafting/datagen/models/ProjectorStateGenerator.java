package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.datagen.models.base.EmptyBlockModelGenerators;

public abstract class ProjectorStateGenerator {

    public static void run(EmptyBlockModelGenerators blockModels) {
//        projectorDishModel();
//        projectorStaticModel();

        var textures = BaseModelHelper.addBase();

        blockModels.createSimpleWithTextures(
                CCBlocks.INACTIVE_FIELD_PROJECTOR_BLOCK,
                BaseModelHelper.projectorBase(),
                textures
        );

        blockModels.createSimpleWithTextures(
                CCBlocks.FIELD_PROJECTOR_BLOCK,
                BaseModelHelper.projectorBase(),
                textures
        );

//        this.getVariantBuilder(CCBlocks.FIELD_PROJECTOR_BLOCK.get())
//                .forAllStates(state -> {
//                    Direction dir = state.getValue(FieldProjectorBlock.FACING);
//                    boolean active = FieldProjectorBlock.isActive(state);
//
//                    if(active) {
//                        return ConfiguredModel.builder()
//                                .modelFile(models().getExistingFile(modLoc("block/base")))
//                                .build();
//                    } else {
//                        return ConfiguredModel.builder()
//                                .modelFile(models().getExistingFile(modLoc("block/field_projector_static")))
//                                .rotationY(((int) dir.toYRot() - 90) % 360)
//                                .build();
//                    }
//                });
    }

//    private void projectorStaticModel() {
//        BlockModelBuilder builder = models().getBuilder("block/field_projector_static")
//                .texture("particle", modLoc("block/projector_base_bottom"));
//                //.renderType(ForgeRenderTypes.getEntityCutoutMipped());
//
//        SharedStateGenerator.addProjectorBase(builder);
//        addDishModel(builder);
//    }

//    private void projectorDishModel() {
//        /**
//         * WEST = FRONT
//         * EAST = BACK
//         * NORTH = LEFT SIDE
//         * SOUTH = EAST SIDE
//         */
//        BlockModelBuilder builder = models().getBuilder("block/field_projector_dish")
//                .texture("particle", modLoc("block/projector_dish_back"));
//
//        // Dish
//        addDishModel(builder);
//    }
//
//    private void addDishModel(BlockModelBuilder builder) {
//        builder
//                .texture("dish_front", modLoc("block/projector_dish_front"))
//                .texture("dish_front_sides", modLoc("block/projector_dish_front_sides"))
//                .texture("dish_back", modLoc("block/projector_dish_back"))
//                .texture("dish_connector", modLoc("block/projector_dish_connector"));
//
//        builder.element()
//                .from(4, 8, 3)
//                .to(6, 16, 13)
//                .allFaces((dir, face) -> {
//                    switch(dir) {
//                        case UP:
//                            face.texture("#dish_front_sides").uvs(1, 0, 11, 1).end();
//                            break;
//
//                        case DOWN:
//                            face.texture("#dish_front_sides").uvs(1, 9, 11, 10).end();
//                            break;
//
//                        case EAST:
//                            face.texture("#dish_back").uvs(0, 0, 10, 8).end();
//                            break;
//
//                        case WEST:
//                            face.texture("#dish_front_sides").uvs(1, 1, 11, 9).end();
//                            break;
//
//                        case NORTH:
//                            face.texture("#dish_front_sides").uvs(0, 1, 1, 9).end();
//                            break;
//
//                        case SOUTH:
//                            face.texture("#dish_front_sides").uvs(11, 1, 12, 9).end();
//                            break;
//                    }
//                })
//                .shade(true)
//                .end();
//
//        // Front texture
//        builder.element()
//                .from(4, 8, 3)
//                .to(5, 16, 13)
//                .allFaces((dir, face) -> {
//                    if(dir == Direction.WEST) {
//                        face.texture("#dish_front").uvs(0, 0, 10, 8).tintindex(0).end();
//                    } else {
//                        face.texture("#dish_front").uvs(0, 0, 1, 1)
//                                .cullface(dir.getOpposite()).end();
//                    }
//                })
//                .end();
//
//        builder.element()
//                .from(6, 11, 7)
//                .to(7, 13, 9)
//                .allFaces((dir, face) -> {
//                    switch(dir) {
//                        case UP:
//                            face.texture("#dish_connector").uvs(1, 0, 3, 1).end();
//                            break;
//
//                        case DOWN:
//                            face.texture("#dish_connector").uvs(1, 3, 3, 4).end();
//                            break;
//
//                        case EAST:
//                            // back
//                            face.texture("#dish_connector").uvs(1, 1, 3, 3).end();
//                            break;
//
//                        case NORTH:
//                            face.texture("#dish_connector").uvs(0, 1, 1, 3).end();
//                            break;
//
//                        case SOUTH:
//                            face.texture("#dish_connector").uvs(3, 1, 4, 3).end();
//                            break;
//                    }
//                })
//                .end();
//    }
}
