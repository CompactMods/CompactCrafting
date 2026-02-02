package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class CCModels {
    public static final TextureSlot BASE_TOP = TextureSlot.create("base_top");
    public static final TextureSlot BASE_TOP_CUTOUT = TextureSlot.create("base_top_cutout");
    public static final TextureSlot BASE_BOTTOM = TextureSlot.create("base_bottom");
    public static final TextureSlot BASE_SIDE = TextureSlot.create("base_side");
    public static final TextureSlot BASE_POLE = TextureSlot.create("pole");

    public static final TextureSlot DISH_TINT = TextureSlot.create("dish_tint");
    public static final TextureSlot DISH_FRONT_SIDES = TextureSlot.create("dish_front_sides");
    public static final TextureSlot DISH_BACK = TextureSlot.create("dish_back");
    public static final TextureSlot DISH_CONNECTOR = TextureSlot.create("dish_connector");

    public static final Set<TextureSlot> BASE_TEXTURES = Set.of(CCModels.BASE_TOP,
            CCModels.BASE_TOP_CUTOUT,
            CCModels.BASE_BOTTOM,
            CCModels.BASE_SIDE,
            CCModels.BASE_POLE);

    public static final Set<TextureSlot> DISH_TEXTURES = Set.of(
            CCModels.DISH_TINT,
            CCModels.DISH_FRONT_SIDES,
            CCModels.DISH_BACK,
            CCModels.DISH_CONNECTOR
    );

    public static final TextureSlot[] STATIC_PROJECTOR_TEXTURE_SLOTS = staticProjectorTextureSlots();

    public static final TextureSlot[] DISH_TEXTURE_SLOTS = dishTextureSlots();

    private static TextureSlot[] staticProjectorTextureSlots() {
        var textures = new HashSet<TextureSlot>();
        textures.add(TextureSlot.PARTICLE);
        textures.addAll(CCModels.BASE_TEXTURES);
        textures.addAll(CCModels.DISH_TEXTURES);
        return textures.toArray(TextureSlot[]::new);
    }

    private static TextureSlot[] dishTextureSlots() {
        var textures = new HashSet<TextureSlot>();
        textures.add(TextureSlot.PARTICLE);
        textures.addAll(CCModels.DISH_TEXTURES);
        return textures.toArray(TextureSlot[]::new);
    }

    public static final ModelTemplate PROJECTOR_BASE = ProjectorBaseModels.makeBaseModel();
    public static final ModelTemplate STATIC_PROJECTOR = makeStaticProjectorModel();
    public static final ModelTemplate DISH = makeDishModel();

    public static final ModelTemplate PROJECTOR_ITEM = ModelTemplates.createItem("projector",
            STATIC_PROJECTOR_TEXTURE_SLOTS);

    public static ModelTemplate makeStaticProjectorModel() {
        final var builder = ModelTemplates.create(
                        CompactCrafting.identifierString("static_projector"),
                        STATIC_PROJECTOR_TEXTURE_SLOTS)
                .extend()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .renderType("cutout");

        ProjectorBaseModels.addBaseGeometry(builder);
        ProjectorDishModels.addDishGeometry(builder);

        return builder.build();
    }

    public static ModelTemplate makeDishModel() {
        final var builder = ModelTemplates.create(
                        CompactCrafting.identifierString("projector_dish"),
                        DISH_TEXTURE_SLOTS)
                .extend()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .renderType("cutout");

        ProjectorDishModels.addDishGeometry(builder);

        return builder.build();
    }
}
