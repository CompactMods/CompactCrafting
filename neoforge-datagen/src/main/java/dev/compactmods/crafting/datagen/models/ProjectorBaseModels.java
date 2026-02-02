package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.Set;

public class ProjectorBaseModels {

    public static TextureMapping addBaseTextures(TextureMapping textureMapping) {
        return textureMapping
                .put(TextureSlot.PARTICLE, CompactCrafting.identifier("block/projector_base_bottom"))
                .put(CCModels.BASE_TOP, CompactCrafting.identifier("block/projector_base_top"))
                .put(CCModels.BASE_TOP_CUTOUT, CompactCrafting.identifier("block/projector_base_top_cutout"))
                .put(CCModels.BASE_BOTTOM, CompactCrafting.identifier("block/projector_base_bottom"))
                .put(CCModels.BASE_SIDE, CompactCrafting.identifier("block/projector_base_side"))
                .put(CCModels.BASE_POLE, CompactCrafting.identifier("block/projector_pole"));
    }

    public static ModelTemplate makeBaseModel() {
        final var builder = ModelTemplates.create(CompactCrafting.identifierString("base"),
                        TextureSlot.PARTICLE,
                        CCModels.BASE_TOP,
                        CCModels.BASE_TOP_CUTOUT,
                        CCModels.BASE_BOTTOM,
                        CCModels.BASE_SIDE,
                        CCModels.BASE_POLE)
                .extend()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .renderType("cutout");

        // Base
        addBaseGeometry(builder);

        return builder.build();
    }

    public static void addBaseGeometry(ExtendedModelTemplateBuilder builder) {
        // Base Base
        builder.element(b -> {
            b.from(0, 0, 0)
                    .to(16, 6, 16)
                    .shade(true);

            b.allFacesExcept((dir, face) -> face.texture(CCModels.BASE_SIDE)
                            .uvs(0, 10, 16, 16),
                    Set.of(Direction.UP, Direction.DOWN));

            b.face(Direction.UP, face -> face.texture(CCModels.BASE_TOP)
                    .uvs(0, 0, 16, 16)
                    .cullface(null));

            b.face(Direction.DOWN, face -> face.texture(CCModels.BASE_BOTTOM)
                    .uvs(0, 0, 16, 16));
        });

        // Base Top - Cutout for colored ring
        builder.element(b -> {
            b.from(0, 6, 0)
                    .to(16, 6, 16)
                    .shade(true);

            b.face(Direction.UP, face -> face.texture(CCModels.BASE_TOP_CUTOUT)
                    .uvs(0, 0, 16, 16)
                    .cullface(null)
                    .lightEmission(2)
                    .tintindex(1));

            b.allFacesExcept((dir, face) -> face.texture(CCModels.BASE_TOP_CUTOUT)
                    .uvs(0, 0, 0, 0)
                    .cullface(dir.getOpposite()), Set.of(Direction.UP));
        });

        // Pole
        builder.element(b -> b
                .from(7, 6, 7)
                .to(9, 12, 9)
                .shade(true)
                .allFacesExcept((dir, face) -> face.texture(CCModels.BASE_POLE).uvs(0, 2, 2, 10),
                        Set.of(Direction.UP, Direction.DOWN))
                .face(Direction.UP, face -> face.texture(CCModels.BASE_POLE).uvs(0, 0, 2, 2))
                .face(Direction.DOWN, down -> down.texture(CCModels.BASE_POLE)));
    }
}
