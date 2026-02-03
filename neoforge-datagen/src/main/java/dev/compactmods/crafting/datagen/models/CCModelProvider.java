package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.datagen.models.base.EmptyBlockModelGenerators;
import dev.compactmods.crafting.datagen.models.base.EmptyItemModelGenerators;
import dev.compactmods.crafting.projector.FieldProjectorsCommon;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

public class CCModelProvider extends ModelProvider {
    public CCModelProvider(PackOutput output) {
        super(output, CompactCrafting.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        final var blocks = new EmptyBlockModelGenerators(blockModels);
        final var items = new EmptyItemModelGenerators(itemModels);

        var textures = new TextureMapping();
        ProjectorBaseModels.addBaseTextures(textures);
        ProjectorDishModels.addDishTextures(textures);

        final var inactiveProjectorModel = CCModels.STATIC_PROJECTOR.create(FieldProjectorsCommon.INACTIVE_FIELD_PROJECTOR_BLOCK.get(),
                textures, blocks.modelOutput);

        blocks.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        FieldProjectorsCommon.INACTIVE_FIELD_PROJECTOR_BLOCK.get(),
                        BlockModelGenerators.plainVariant(inactiveProjectorModel)
                ).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        blocks.createSimpleWithTextures(
                FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK,
                CCModels.PROJECTOR_BASE,
                textures
        );

        var dishTextures = new TextureMapping();
        ProjectorDishModels.addDishTextures(dishTextures);

        final var dishModelId = CCModels.DISH.create(CompactCrafting.identifier("projector_dish"),
                dishTextures, blocks.modelOutput);

        // Missing item model definitions for: [compactcrafting:field_projector, compactcrafting:projector_dish]
//        itemModels.generateFlatItem(CCItems.FIELD_PROJECTOR_ITEM.get(), ModelTemplates.FLAT_ITEM);

        blocks.registerSimpleItemModel(FieldProjectorsCommon.FIELD_PROJECTOR_ITEM.get(), inactiveProjectorModel);

        var base = blocks.createFlatItemModel(FieldProjectorsCommon.BASE_ITEM.get());
        blocks.registerSimpleItemModel(FieldProjectorsCommon.BASE_ITEM.get(), base);

        blocks.registerSimpleItemModel(FieldProjectorsCommon.PROJECTOR_DISH_ITEM.get(), dishModelId);

        blocks.run();
        items.run();
    }
}
