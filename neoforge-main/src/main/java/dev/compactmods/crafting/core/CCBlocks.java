package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CCBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactCrafting.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactCrafting.MOD_ID);

    public static final DeferredBlock<FieldProjectorBlock> FIELD_PROJECTOR_BLOCK = BLOCKS.register("field_projector", () ->
            new FieldProjectorBlock(BlockBehaviour.Properties.of()
                    .strength(8, 20)
                    .isRedstoneConductor((state, level, pos) -> true)
                    .requiresCorrectToolForDrops()
            ));

//    static final Supplier<BlockBehaviour.Properties> PROXY_PROPS = () -> BlockBehaviour.Properties.of()
//            .strength(8, 20)
//            .requiresCorrectToolForDrops();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldProjectorEntity>> FIELD_PROJECTOR_TILE = BLOCK_ENTITIES.register("field_projector", () ->
            BlockEntityType.Builder
                    .of(FieldProjectorEntity::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
