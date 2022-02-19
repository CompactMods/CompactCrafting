package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.tests.projectors.Projectors;
import dev.compactmods.crafting.tests.recipes.components.CCRecipeComponentsTests;
import dev.compactmods.crafting.tests.recipes.components.ComponentTypeCodecTests;
import dev.compactmods.crafting.tests.recipes.components.EmptyComponentTests;
import dev.compactmods.crafting.tests.recipes.data.MiniaturizationRecipeCodecTests;
import dev.compactmods.crafting.tests.recipes.data.MiniaturizationRecipeSerializerTests;
import dev.compactmods.crafting.tests.recipes.layers.*;
import dev.compactmods.crafting.tests.recipes.setup.RecipeSetupTests;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestsMain {

    @SubscribeEvent
    public static void registerTests(final RegisterGameTestsEvent game) {
        CompactCrafting.LOGGER.debug("Registering game tests.");

//        try {
//            var f = new File("test-report.xml");
//            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(f));
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }

        // projectors
        game.register(Projectors.class);

        // recipes.components
        game.register(CCRecipeComponentsTests.class);
        game.register(ComponentTypeCodecTests.class);
        game.register(EmptyComponentTests.class);

        // recipes.data
        game.register(MiniaturizationRecipeCodecTests.class);
        game.register(MiniaturizationRecipeSerializerTests.class);

        // recipes.layers
        game.register(FilledLayerTests.class);
        game.register(HollowLayer.class);
        game.register(MixedLayerTests.class);
        game.register(RecipeBlocksTests.class);
        game.register(RecipeLayerTypeCodecTests.class);
        game.register(RecipeLayerUtilTests.class);

        // recipes.setup
        game.register(RecipeSetupTests.class);
    }
}
