package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public interface FieldProjectorsCommon {
    DeferredBlock<OfflineFieldProjectorBlock> INACTIVE_FIELD_PROJECTOR_BLOCK = CompactCraftingCommon.BLOCKS
            .registerBlock("inactive_field_projector", props -> new OfflineFieldProjectorBlock(props
                    .strength(8, 20)
                    .pushReaction(PushReaction.NORMAL)
                    .requiresCorrectToolForDrops()
            ));

    DeferredBlock<ActiveFieldProjectorBlock> FIELD_PROJECTOR_BLOCK = CompactCraftingCommon.BLOCKS
            .registerBlock("field_projector", props -> new ActiveFieldProjectorBlock(props
                    .strength(8, 20)
                    .isRedstoneConductor((state, level, pos) -> true)
                    .pushReaction(PushReaction.NORMAL)
                    .requiresCorrectToolForDrops()
            ));

    DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldProjectorEntity>> FIELD_PROJECTOR_TILE =
            CompactCraftingCommon.BLOCK_ENTITIES.register("field_projector", () ->
                    new BlockEntityType<>(FieldProjectorEntity::new, FIELD_PROJECTOR_BLOCK.get()));

    // ================================================================================================================
    DeferredItem<Item> FIELD_PROJECTOR_ITEM = CompactCraftingCommon.ITEMS
            .registerItem("field_projector", FieldProjectorItem::new);

    DeferredItem<Item> PROJECTOR_DISH_ITEM = CompactCraftingCommon.ITEMS.registerSimpleItem("projector_dish");

    DeferredItem<Item> BASE_ITEM = CompactCraftingCommon.ITEMS.registerSimpleItem("base");
    DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PLACEMENT_TIMER = CompactCraftingCommon.ATTACHMENT_TYPES
            .register("placement_timer", () -> AttachmentType.builder(() -> ClientConfig.placementTime).build());
    DeferredHolder<AttachmentType<?>, AttachmentType<ObjectArrayList<GhostProjectorPlacementRenderer>>> PLACEMENT_HELPERS = CompactCraftingCommon.ATTACHMENT_TYPES
            .register("placement_helpers", () -> AttachmentType
                    .builder(() -> new ObjectArrayList<GhostProjectorPlacementRenderer>())
                    .build());

    static void prepare() {
    }
}
