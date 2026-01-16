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

@EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        final var generator = event.getGenerator();

        final var packOutput = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        event.addProvider(new LootTableProvider(packOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)),
                holderLookup
        ));


        event.createProvider((output,provider)
                -> new RecipeGenerator.Runner(CompactCrafting.rlPrefix("base"), output, provider));

//        generator.addProvider(event.includeClient(), new SharedStateGenerator(pack, event.getExistingFileHelper()));
//        generator.addProvider(event.includeClient(), new ProjectorStateGenerator(pack, event.getExistingFileHelper()));
//        generator.addProvider(event.includeClient(), new ProxyStateGenerator(generator, event.getExistingFileHelper()));
    }
}
