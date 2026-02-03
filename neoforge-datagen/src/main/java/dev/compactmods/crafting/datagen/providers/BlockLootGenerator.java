package dev.compactmods.crafting.datagen.providers;

import com.google.common.collect.ImmutableList;
import dev.compactmods.crafting.projector.FieldProjectorsCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;

public class BlockLootGenerator extends BlockLootSubProvider {

    public BlockLootGenerator(HolderLookup.Provider holderLookup) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), holderLookup);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ImmutableList.of(
                FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.get()
//                CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(),
//                CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get()
        );
    }

    @Override
    protected void generate() {
        this.add(FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.get(), LootTable.lootTable().withPool(LootPool
                .lootPool()
                .name(FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(FieldProjectorsCommon.FIELD_PROJECTOR_ITEM.get()))));
    }
}
