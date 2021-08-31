package com.robotgryphon.compactcrafting.events;

import com.robotgryphon.compactcrafting.field.FieldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.robotgryphon.compactcrafting.CompactCrafting.MOD_ID;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class EventHandler {
    
    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent blockPlaced) {
        // Check if block is in or around a projector field
        IWorld world = blockPlaced.getWorld();
        BlockPos pos = blockPlaced.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        if(world instanceof World)
            FieldHelper.checkBlockPlacement((World) world, pos);
    }

    @SubscribeEvent
    public static void onBlockDestroyed(final BlockEvent.BreakEvent blockDestroyed) {
        // Check if block is in or around a projector field
        IWorld world = blockDestroyed.getWorld();
        BlockPos pos = blockDestroyed.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        if(world instanceof World)
            FieldHelper.checkBlockPlacement((World) world, pos);
    }
}
