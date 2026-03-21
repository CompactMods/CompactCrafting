package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.client.CompactCraftingClient;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.Set;

public abstract class ProjectorDishModels {

    public static TextureMapping addDishTextures(TextureMapping mapping) {
        return mapping
                .put(TextureSlot.PARTICLE, CompactCraftingClient.material("block/projector_dish_back"))
                .put(CCModels.DISH_TINT, CompactCraftingClient.material("block/projector_dish_tint"))
                .put(CCModels.DISH_FRONT_SIDES, CompactCraftingClient.material("block/projector_dish_front_sides"))
                .put(CCModels.DISH_BACK, CompactCraftingClient.material("block/projector_dish_back"))
                .put(CCModels.DISH_CONNECTOR, CompactCraftingClient.material("block/projector_dish_connector"));
    }

    public static void addDishGeometry(ExtendedModelTemplateBuilder builder) {
        builder.element(b -> b.from(3, 8, 4)
                .to(13, 16, 6)
                .rotation(rot -> rot.origin(3, 8, 4).singleAxis(Direction.Axis.Y, 0))
                .shade(true)
                .allFaces((dir, face) -> {
                    switch (dir) {
                        case UP -> face.texture(CCModels.DISH_FRONT_SIDES).uvs(3, 1, 13, 3);
                        case DOWN -> face.texture(CCModels.DISH_FRONT_SIDES).uvs(3, 13, 13, 15);
                        case EAST -> face.texture(CCModels.DISH_FRONT_SIDES).uvs(14, 4, 16, 12);
                        case WEST -> face.texture(CCModels.DISH_FRONT_SIDES).uvs(0, 4, 2, 12);
                        case NORTH -> face.texture(CCModels.DISH_FRONT_SIDES).uvs(3, 4, 13, 12);
                        case SOUTH -> face.texture(CCModels.DISH_BACK).uvs(3, 4, 13, 12);
                    }
                }));

        // Tint Cutout texture
        builder.element(b -> b.from(4, 9, 4)
                .to(12, 15, 5)
                .textureAll(CCModels.DISH_TINT)
                .rotation(rot -> rot.origin(5, 8, 4).singleAxis(Direction.Axis.Y, 0))
                .face(Direction.NORTH, face -> face.uvs(4, 5, 12, 11)
                        .tintindex(0))

                .allFacesExcept((dir, face) -> face
                        .uvs(0, 0, 1, 1)
                        .cullface(dir.getOpposite()), Set.of(Direction.NORTH)));

        builder.element(b -> b.from(7, 11, 6)
                .to(9, 13, 7)
                .face(Direction.DOWN, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(1, 3, 3, 4))
                .face(Direction.UP, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(1, 0, 3, 1))
                .face(Direction.SOUTH, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(1, 1, 3, 3))
                .face(Direction.NORTH, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(1, 1, 3, 3))
                .face(Direction.WEST, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(0, 1, 1, 3))
                .face(Direction.EAST, face -> face.texture(CCModels.DISH_CONNECTOR).uvs(3, 1, 4, 3)));
    }
}
