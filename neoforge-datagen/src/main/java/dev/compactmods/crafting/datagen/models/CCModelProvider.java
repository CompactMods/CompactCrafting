package dev.compactmods.crafting.datagen.models;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.datagen.models.base.EmptyBlockModelGenerators;
import dev.compactmods.crafting.datagen.models.base.EmptyItemModelGenerators;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class CCModelProvider extends ModelProvider {
    public CCModelProvider(PackOutput output) {
        super(output, CompactCrafting.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        final var blocks = new EmptyBlockModelGenerators(blockModels);
        final var items = new EmptyItemModelGenerators(itemModels);

        ProjectorStateGenerator.run(blocks);

        // Missing item model definitions for: [compactcrafting:field_projector, compactcrafting:projector_dish]
//        itemModels.generateFlatItem(CCItems.FIELD_PROJECTOR_ITEM.get(), ModelTemplates.FLAT_ITEM);
//
        items.generateFlatItem(CCItems.FIELD_PROJECTOR_ITEM.get(), Items.DIAMOND, ModelTemplates.FLAT_ITEM);
        items.generateFlatItem(CCItems.BASE_ITEM.get(), Items.DIAMOND, ModelTemplates.FLAT_ITEM);
        items.generateFlatItem(CCItems.PROJECTOR_DISH_ITEM.get(), Items.DIAMOND, ModelTemplates.FLAT_ITEM);

//                .withExistingParent("projector_dish", modLoc())
//                .transforms()
//                .transform(ItemDisplayContext.GUI)
//                .rotation(33.75f, 45f, 0)
//                .translation(2, -2, 0)
//                .scale(1f, 1f, 1f)
//                .end();
//
//        itemModels()
//                .withExistingParent("field_projector", modLoc("block/field_projector_static"))
//                .transforms()
//                .transform(ItemDisplayContext.GUI)
//                .rotation(33.75f, 45f, 0)
//                .translation(0, 1, 0)
//                .scale(0.6f, 0.6f, 0.6f)
//                .end();

        blocks.run();
        items.run();
    }
}
