package dev.compactmods.crafting.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCCapabilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FieldInfoCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create() {

        var field = Commands.literal("field");

        field.then(Commands.literal("ref")
                .then(Commands.argument("block", BlockPosArgument.blockPos())
                        .executes(FieldInfoCommand::byReference)));

        field.then(Commands.literal("center")
                .then(Commands.argument("center", BlockPosArgument.blockPos())
                        .executes(FieldInfoCommand::byCenter)));

        return field;
    }

    private static int byCenter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "center");
        final CommandSourceStack src = ctx.getSource();
        final ServerLevel level = src.getLevel();

        level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
            if (!fields.hasActiveField(pos)) {
                src.sendFailure(new TranslatableComponent("messages." + CompactCrafting.MOD_ID + ".no_field_found", pos));
                return;
            }

            fields.get(pos).ifPresent(field -> outputStdFieldInfo(src, field));
        });

        return 0;
    }

    private static int byReference(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "block");
        final CommandSourceStack src = ctx.getSource();
        final ServerLevel level = src.getLevel();

        if (level.getBlockState(pos).hasBlockEntity()) {
            final BlockEntity ent = level.getBlockEntity(pos);
            if (ent == null) {
                src.sendFailure(new TranslatableComponent("messages." + CompactCrafting.MOD_ID + ".no_field_cap", pos));
                return 0;
            }

            ent.getCapability(CCCapabilities.MINIATURIZATION_FIELD).ifPresent(field -> outputStdFieldInfo(src, field));
        } else {
            src.sendFailure(new TranslatableComponent("messages." + CompactCrafting.MOD_ID + ".no_field_cap", pos));
        }

        return 0;
    }

    private static void outputStdFieldInfo(CommandSourceStack src, IMiniaturizationField field) {
        src.sendSuccess(new TextComponent("Center: " + field.getCenter().toString()), false);
        src.sendSuccess(new TextComponent("Size: " + field.getFieldSize().getName()), false);
        field.getCurrentRecipe().ifPresent(rec -> {
            src.sendSuccess(new TextComponent("Recipe: " + rec.getRecipeIdentifier()), false);
        });
    }
}
