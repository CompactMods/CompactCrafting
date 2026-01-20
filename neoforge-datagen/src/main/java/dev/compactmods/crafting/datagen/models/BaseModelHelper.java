package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.FaceBuilder;

import java.util.Collections;
import java.util.Set;

public class BaseModelHelper {

    public static TextureMapping addBase() {
        return new TextureMapping()
                .put(TextureSlot.PARTICLE, CompactCrafting.identifier("block/projector_base_bottom"))
                .put(CCModels.BASE_TOP, CompactCrafting.identifier("block/projector_base_top"))
                .put(CCModels.BASE_TOP_CUTOUT, CompactCrafting.identifier("block/projector_base_top_cutout"))
                .put(CCModels.BASE_BOTTOM, CompactCrafting.identifier("block/projector_base_bottom"))
                .put(CCModels.BASE_SIDE, CompactCrafting.identifier("block/projector_base_side"))
                .put(CCModels.POLE, CompactCrafting.identifier("block/projector_pole"));
    }


    public static ModelTemplate projectorBase() {
        final var builder = ModelTemplates.create(CompactCrafting.identifierString("base"),
                        CCModels.BASE_TOP,
                        CCModels.BASE_TOP_CUTOUT,
                        CCModels.BASE_BOTTOM,
                        CCModels.BASE_SIDE,
                        CCModels.POLE)
                .extend()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .renderType("cutout");

        // Base
        addBase(builder);

        builder.element(b -> b
                .from(7, 6, 7)
                .to(9, 12, 9)
                .shade(true)
                .allFacesExcept(BaseModelHelper::poleTextures, Collections.singleton(Direction.DOWN))
                .face(Direction.DOWN, down -> down.texture(CCModels.POLE)));

        return builder.build();
    }

    private static void addBase(ExtendedModelTemplateBuilder builder) {
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
    }

    private static void poleTextures(Direction dir, FaceBuilder face) {
        switch (dir) {
            case NORTH, SOUTH, WEST, EAST -> face.texture(CCModels.POLE).uvs(0, 2, 2, 10);
            case UP -> face.texture(CCModels.POLE).uvs(0, 0, 2, 2);
        }
    }
}
