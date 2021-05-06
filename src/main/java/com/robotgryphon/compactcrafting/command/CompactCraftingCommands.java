package com.robotgryphon.compactcrafting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CompactCraftingCommands {
    private CompactCraftingCommands() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> base = Commands.literal(CompactCrafting.MOD_ID);

        RecipeCopyCommand.register(base);
        RecipeViewCommand.register(base);

        dispatcher.register(base);
    }
}
