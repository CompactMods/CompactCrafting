package com.robotgryphon.compactcrafting.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.util.GuiUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class RecipeViewCommand {
    private RecipeViewCommand() {}

    public static void register(LiteralArgumentBuilder<CommandSource> base) {
        base.then(Commands.literal("recipe")
                .then(Commands.literal("view")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((context, builder) ->
                                        ISuggestionProvider.suggestResource(RecipeHelper.getLoadedRecipes(context.getSource().getLevel()).stream()
                                                .map(IRecipe::getId), builder))
                                .executes(RecipeViewCommand::run))));
    }

    private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        CommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrException();

        ResourceLocation id = ResourceLocationArgument.getId(context, "id");
        MiniaturizationRecipe recipe = getRecipe(player, id);
        if (recipe == null) {
            source.sendFailure(new TranslationTextComponent("commands.compactcrafting.recipe.view.invalid"));
            return 0;
        }

        GuiUtil.openMiniaturizationGui(player, recipe, false);

        return 1;
    }

    private static MiniaturizationRecipe getRecipe(ServerPlayerEntity player, ResourceLocation id) {
        if (id == null)
            return null;

        return RecipeHelper.getLoadedRecipes(player.getCommandSenderWorld())
                .stream()
                .filter(r -> r.getId().equals(id))
                .findAny()
                .orElse(null);
    }
}
