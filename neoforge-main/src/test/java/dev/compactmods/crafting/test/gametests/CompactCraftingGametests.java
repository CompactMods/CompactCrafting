package dev.compactmods.crafting.test.gametests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;

@Mod(CompactCrafting.MOD_ID)
public class CompactCraftingGametests {

    public CompactCraftingGametests(ModContainer container, IEventBus modBus) {
        final var config = FrameworkConfiguration.builder(CompactCrafting.modRL("tests"))
                .enable(Feature.GAMETEST)
                .enable(Feature.MAGIC_ANNOTATIONS)
                .build();

        var fw = config.create();
        fw.registerCommands(Commands.literal("cctest"));
        fw.init(modBus, container);
    }
}
