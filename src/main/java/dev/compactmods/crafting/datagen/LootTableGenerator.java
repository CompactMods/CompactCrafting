package dev.compactmods.crafting.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.compactmods.crafting.Registration;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableGenerator extends LootTableProvider {

    public LootTableGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((name, table) -> LootTableManager.validate(validationtracker, name, table));
    }

    private static class Blocks extends BlockLootTables {
        @Override
        protected void addTables() {
            registerSelfDroppedBlock(Registration.FIELD_PROJECTOR_BLOCK, Registration.FIELD_PROJECTOR_ITEM);
        }

        private LootPool.Builder registerSelfDroppedBlock(RegistryObject<Block> block, RegistryObject<Item> item) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.get().getRegistryName().toString())
                    .setRolls(ConstantRange.exactly(1))
                    .when(SurvivesExplosion.survivesExplosion())
                    .add(ItemLootEntry.lootTableItem(item.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
            return builder;
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(
                    Registration.FIELD_PROJECTOR_BLOCK.get()
            );
        }
    }
}
