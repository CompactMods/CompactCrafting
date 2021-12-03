package dev.compactmods.crafting.datagen;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.compactmods.crafting.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

public class LootTableGenerator extends LootTableProvider {

    public LootTableGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        map.forEach((name, table) -> LootTables.validate(validationtracker, name, table));
    }

    private static class Blocks extends BlockLoot {
        @Override
        protected void addTables() {
            registerSelfDroppedBlock(Registration.FIELD_PROJECTOR_BLOCK, Registration.FIELD_PROJECTOR_ITEM);
            registerSelfDroppedBlock(Registration.MATCH_FIELD_PROXY_BLOCK, Registration.MATCH_PROXY_ITEM);
            registerSelfDroppedBlock(Registration.RESCAN_FIELD_PROXY_BLOCK, Registration.RESCAN_PROXY_ITEM);
        }

        private LootPool.Builder registerSelfDroppedBlock(RegistryObject<Block> block, RegistryObject<Item> item) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.get().getRegistryName().toString())
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .add(LootItem.lootTableItem(item.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
            return builder;
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(
                    Registration.FIELD_PROJECTOR_BLOCK.get(),
                    Registration.MATCH_FIELD_PROXY_BLOCK.get(),
                    Registration.RESCAN_FIELD_PROXY_BLOCK.get()
            );
        }
    }
}
