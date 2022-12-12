package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.datagen.tags.BlockTagGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        if (event.includeServer())
            registerServerProviders(event.getGenerator(), event);

        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) {
        final var files = event.getExistingFileHelper();

        generator.addProvider(new LootTableGenerator(generator));
        generator.addProvider(new RecipeGenerator(generator));

        var blockTags = new BlockTagGenerator(generator, files);
        generator.addProvider(blockTags);
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        generator.addProvider(new SharedStateGenerator(generator, event.getExistingFileHelper()));
        generator.addProvider(new ProjectorStateGenerator(generator, event.getExistingFileHelper()));
        generator.addProvider(new ProxyStateGenerator(generator, event.getExistingFileHelper()));
    }
}
