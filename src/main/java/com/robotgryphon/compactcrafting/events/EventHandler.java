package com.robotgryphon.compactcrafting.events;

import com.robotgryphon.compactcrafting.field.FieldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.robotgryphon.compactcrafting.CompactCrafting.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class EventHandler {

    @SubscribeEvent
    public static void onBlockPlaced(final net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent blockPlaced) {
        // Check if block is in or around a projector field

        IWorld world = blockPlaced.getWorld();
        BlockPos pos = blockPlaced.getPos();

        // We don't care about client worlds RN
        if(world.isRemote())
            return;

        // Send the event position over to the field helper, so any nearby projectors can be notified
        FieldHelper.checkBlockPlacement(world, pos);
    }
}
