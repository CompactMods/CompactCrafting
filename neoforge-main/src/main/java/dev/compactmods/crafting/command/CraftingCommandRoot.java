package dev.compactmods.crafting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.crafting.CompactCrafting;
import io.reactivex.rxjava3.disposables.Disposable;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingCommandRoot {

    static Disposable PREV;

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(CompactCrafting.MOD_ID);

        dispatcher.register(root);
    }

}
