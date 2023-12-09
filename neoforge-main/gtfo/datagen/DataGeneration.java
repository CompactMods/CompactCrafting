package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        generator.addProvider(event.includeServer(), new LootTableGenerator(generator));
        generator.addProvider(event.includeServer(), new RecipeGenerator(generator));
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        generator.addProvider(event.includeClient(), new SharedStateGenerator(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ProjectorStateGenerator(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ProxyStateGenerator(generator, event.getExistingFileHelper()));
    }
}
