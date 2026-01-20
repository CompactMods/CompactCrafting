package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.projector.ActiveFieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.projector.OfflineFieldProjectorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface CCBlocks {

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

//    static final Supplier<BlockBehaviour.Properties> PROXY_PROPS = () -> BlockBehaviour.Properties.of()
//            .strength(8, 20)
//            .requiresCorrectToolForDrops();

    DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldProjectorEntity>> FIELD_PROJECTOR_TILE =
            CompactCraftingCommon.BLOCK_ENTITIES.register("field_projector", () ->
                new BlockEntityType<>(FieldProjectorEntity::new, FIELD_PROJECTOR_BLOCK.get()));

    static void init(IEventBus bus) {
        CompactCraftingCommon.BLOCKS.register(bus);
        CompactCraftingCommon.BLOCK_ENTITIES.register(bus);
    }
}
