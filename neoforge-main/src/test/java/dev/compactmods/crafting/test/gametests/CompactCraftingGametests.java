package dev.compactmods.crafting.test.gametests;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.test.gametests.util.CompactGameTestHelper;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.GameTestHelperFactory;

@Mod(CompactCrafting.MOD_ID)
public class CompactCraftingGametests {

    public CompactCraftingGametests(ModContainer container, IEventBus modBus) {

        // GameTestHelperFactory.CONSTRUCTORS.put(CompactGameTestHelper.class, CompactGameTestHelper::new);

        final var config = FrameworkConfiguration.builder(CompactCrafting.modRL("tests"))
                .enable(Feature.GAMETEST)
                .enable(Feature.MAGIC_ANNOTATIONS)
                .build();

        var fw = config.create();
        fw.registerCommands(Commands.literal("cctest"));
        fw.init(modBus, container);
    }
}
