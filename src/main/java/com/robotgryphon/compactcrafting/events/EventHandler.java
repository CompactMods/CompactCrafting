package com.robotgryphon.compactcrafting.events;

import com.mojang.brigadier.CommandDispatcher;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.command.CompactCraftingCommands;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.field.FieldHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class EventHandler {
    private EventHandler() {}

    // TODO - Tinker with sending recipe updates at a further timespan away
    // This is working with BG now, need to figure out why recipe isn't being matched
    
    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent blockPlaced) {
        // Check if block is in or around a projector field

        IWorld world = blockPlaced.getWorld();
        BlockPos pos = blockPlaced.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        FieldHelper.checkBlockPlacement(world, pos, BlockUpdateType.PLACE);
    }

    @SubscribeEvent
    public static void onBlockDestroyed(final BlockEvent.BreakEvent blockDestroyed) {
        // Check if block is in or around a projector field

        IWorld world = blockDestroyed.getWorld();
        BlockPos pos = blockDestroyed.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        FieldHelper.checkBlockPlacement(world, pos, BlockUpdateType.REMOVE);
    }

    @SubscribeEvent
    public static void onCommandRegister(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

        CompactCraftingCommands.register(dispatcher);
    }
}
