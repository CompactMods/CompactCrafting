package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.tests.projectors.Projectors;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestsMain {

    @SubscribeEvent
    public static void registerTests(final RegisterGameTestsEvent game) {
        CompactCrafting.LOGGER.debug("Registering game tests.");

        game.register(Projectors.class);
    }
}
