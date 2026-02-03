package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.datagen.models.CCModelProvider;
import dev.compactmods.crafting.datagen.providers.BlockLootGenerator;
import dev.compactmods.crafting.datagen.providers.BlockTagGenerator;
import dev.compactmods.crafting.datagen.providers.GameEventTagsProvider;
import dev.compactmods.crafting.datagen.providers.RecipeGenerator;
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

        event.createProvider(BlockTagGenerator::new);

        event.addProvider(new LootTableProvider(packOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)),
                holderLookup
        ));

        event.createProvider(GameEventTagsProvider::new);
        event.createProvider(CCModelProvider::new);

        event.createProvider((output,provider)
                -> new RecipeGenerator.Runner(CompactCrafting.identifierString("base"), output, provider));

//        generator.addProvider(event.includeClient(), new ProxyStateGenerator(generator, event.getExistingFileHelper()));
    }
}
