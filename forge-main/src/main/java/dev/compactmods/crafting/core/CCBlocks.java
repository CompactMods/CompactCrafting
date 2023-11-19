package dev.compactmods.crafting.core;

import java.util.function.Supplier;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.proxies.block.MatchFieldProxyBlock;
import dev.compactmods.crafting.proxies.block.RescanFieldProxyBlock;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CCBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CompactCrafting.MOD_ID);

    public static final RegistryObject<Block> FIELD_PROJECTOR_BLOCK = BLOCKS.register("field_projector", () ->
            new FieldProjectorBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(8, 20)
                    .isRedstoneConductor((state, level, pos) -> true)
            ));

    static final Supplier<BlockBehaviour.Properties> PROXY_PROPS = () -> BlockBehaviour.Properties.of(Material.HEAVY_METAL)
            .strength(8, 20);

    public static final RegistryObject<Block> RESCAN_FIELD_PROXY_BLOCK = BLOCKS.register("rescan_proxy", () ->
            new RescanFieldProxyBlock(PROXY_PROPS.get()));

    public static final RegistryObject<Block> MATCH_FIELD_PROXY_BLOCK = BLOCKS.register("match_proxy", () ->
            new MatchFieldProxyBlock(PROXY_PROPS.get()));

    public static final RegistryObject<BlockEntityType<FieldProjectorEntity>> FIELD_PROJECTOR_TILE = BLOCK_ENTITIES.register("field_projector", () ->
            BlockEntityType.Builder
                    .of(FieldProjectorEntity::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<RescanFieldProxyEntity>> RESCAN_PROXY_ENTITY = BLOCK_ENTITIES.register("rescan_proxy", () ->
            BlockEntityType.Builder
                    .of(RescanFieldProxyEntity::new, RESCAN_FIELD_PROXY_BLOCK.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MatchFieldProxyEntity>> MATCH_PROXY_ENTITY = BLOCK_ENTITIES.register("match_proxy", () ->
            BlockEntityType.Builder
                    .of(MatchFieldProxyEntity::new, MATCH_FIELD_PROXY_BLOCK.get())
                    .build(null));

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
