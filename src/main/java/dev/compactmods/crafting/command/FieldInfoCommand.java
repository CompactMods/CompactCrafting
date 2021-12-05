package dev.compactmods.crafting.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class ProjectorInfoCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create() {

        var fieldInfo = Commands.literal("fieldinfo");
        fieldInfo.then(Commands.argument("block", BlockPosArgument.blockPos())
                .executes(ProjectorInfoCommand::exe));

        return fieldInfo;
    }

    private static int exe(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "block");
        final CommandSourceStack src = ctx.getSource();
        final ServerLevel level = src.getLevel();

        if (level.getBlockState(pos).getBlock() instanceof FieldProjectorBlock fpb) {
            if (level.getBlockEntity(pos) instanceof FieldProjectorEntity proj) {
                proj.getField().ifPresent(field -> {
                    src.sendSuccess(new TextComponent("Center: " + field.getCenter().toString()), false);
                    src.sendSuccess(new TextComponent("Size: " + field.getFieldSize().getName()), false);
                    field.getCurrentRecipe().ifPresent(rec -> {
                        src.sendSuccess(new TextComponent("Recipe: " + rec.getRecipeIdentifier()), false);
                    });
                });
            }
        } else {
            src.sendFailure(new TranslatableComponent("messages." + CompactCrafting.MOD_ID + ".not_a_projector", pos));
        }

        return 0;
    }
}
