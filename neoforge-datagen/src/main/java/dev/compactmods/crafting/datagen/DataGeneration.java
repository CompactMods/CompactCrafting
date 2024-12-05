package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer())
            registerServerProviders(event.getGenerator(), event);

        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) {
        var pack = generator.getPackOutput();
        var lookup = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new LootTableProvider(pack,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)),
                lookup
        ));

        generator.addProvider(event.includeServer(), new RecipeGenerator(pack, lookup));
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        var pack = generator.getPackOutput();
        var lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new SharedStateGenerator(pack, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ProjectorStateGenerator(pack, event.getExistingFileHelper()));
//        generator.addProvider(event.includeClient(), new ProxyStateGenerator(generator, event.getExistingFileHelper()));
    }
}
