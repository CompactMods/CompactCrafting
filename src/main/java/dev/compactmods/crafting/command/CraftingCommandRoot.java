package dev.compactmods.crafting.command;

import java.util.concurrent.TimeUnit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.crafting.CompactCrafting;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingCommandRoot {

    static Disposable PREV;

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSource> dispatcher) {
        final LiteralArgumentBuilder<CommandSource> root = LiteralArgumentBuilder.literal(CompactCrafting.MOD_ID);
        root.then(Commands.literal("test")
                .requires(cs -> cs.hasPermission(2))
                .executes(CraftingCommandRoot::test));
        dispatcher.register(root);
    }

    private static int test(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayerOrException();

        if(PREV != null && !PREV.isDisposed()) {
            PREV.dispose();
            PREV = null;
        }
        ctx.getSource().getServer().submitAsync(() -> {

            PublishSubject<Integer> l = PublishSubject.create();
            PREV = l.buffer(500, TimeUnit.MILLISECONDS)
                    .subscribe(times -> {
                        if (!times.isEmpty())
                            player.sendMessage(new StringTextComponent(String.join(",", times.stream()
                                    .map(Object::toString).toArray(String[]::new))), ChatType.CHAT, player.getUUID());
                    }, err -> {
                        CompactCrafting.LOGGER.debug("error");
                    }, () -> {
                        player.sendMessage(new StringTextComponent("done"), ChatType.CHAT, player.getUUID());
                    });

            player.server.submitAsync(() -> {
                for (int i = 1; i <= 100; i++) {
                    l.onNext(i);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return 0;
    }
}
