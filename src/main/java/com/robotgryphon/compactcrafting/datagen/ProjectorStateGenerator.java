package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ProjectorStateGenerator extends BlockStateProvider {

    public ProjectorStateGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen, CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {

        projectorBaseModel();
        projectorDishModel();

//        this.getVariantBuilder(Registration.FIELD_PROJECTOR_BLOCK.get())
//                .forAllStates(state -> {
//                    Direction dir = state.getValue(FieldProjectorBlock.FACING);
//                    return ConfiguredModel.builder()
//                            .modelFile(modelFunc.apply(state))
//                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
//                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + angleOffset) % 360)
//                            .build();
//                })
    }

    private void projectorBaseModel() {
        BlockModelBuilder builder = models().getBuilder("block/field_projector")
                .texture("base_top", modLoc("block/projector_base_top"))
                .texture("base_bottom", modLoc("block/projector_base_bottom"))
                .texture("base_side", modLoc("block/projector_base_side"))
                .texture("dish_front", modLoc("block/projector_dish_front"))
                .texture("dish_front_sides", modLoc("block/projector_dish_front_sides"))
                .texture("dish_back", modLoc("block/projector_dish_back"))
                .texture("pole", modLoc("block/projector_pole"))
                .texture("particle", modLoc("block/projector_base_bottom"));

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

    private void projectorDishModel() {
        /**
         * "elements": [
         *     {
         *       "name": "Dish",
         *       "faces": {
         *         "north": {
         *           "uv": [12.5, 8, 13.5, 11.5],
         *           "texture": "#0"
         *         },
         *         "east": {
         *           "uv": [8, 12, 13, 16],
         *           "texture": "#0"
         *         },
         *         "south": {
         *           "uv": [12.5, 8, 13.5, 11.5],
         *           "texture": "#0"
         *         },
         *         "west": {
         *           "uv": [8, 8, 13, 12],
         *           "texture": "#0",
         *           "tintindex": 0
         *         },
         *         "up": {
         *           "uv": [8, 11.5, 13, 12.5],
         *           "texture": "#0",
         *           "rotation": 90
         *         },
         *         "down": {
         *           "uv": [8, 11.5, 13, 12.5],
         *           "texture": "#0",
         *           "rotation": 90
         *         }
         *       }
         *     },
         *     {
         *       "name": "Connector",
         *       "from": [6, 11, 7],
         *       "to": [7, 13, 9],
         *       "shade": true,
         *       "faces": {
         *         "north": {
         *           "uv": [4.5, 10, 5, 11],
         *           "texture": "#0"
         *         },
         *         "east": {
         *           "uv": [4, 10, 5, 11],
         *           "texture": "#0"
         *         },
         *         "south": {
         *           "uv": [4.5, 10, 5, 11],
         *           "texture": "#0"
         *         },
         *         "west": {
         *           "uv": [0, 0, 2, 2],
         *           "texture": "#0"
         *         },
         *         "up": {
         *           "uv": [4.5, 10, 5, 11],
         *           "texture": "#0"
         *         },
         *         "down": {
         *           "uv": [4.5, 10, 5, 11],
         *           "texture": "#0"
         *         }
         *       }
         *     }
         *   ],
         */
        BlockModelBuilder builder = models().getBuilder("block/field_projector_dish")
                .texture("dish_front", modLoc("block/projector_dish_front"))
                .texture("dish_front_sides", modLoc("block/projector_dish_front_sides"))
                .texture("dish_back", modLoc("block/projector_dish_back"))
                .texture("pole", modLoc("block/projector_pole"))
                .texture("particle", modLoc("block/projector_base_bottom"));

        // Dish
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
    }
}
