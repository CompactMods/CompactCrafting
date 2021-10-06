package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import static dev.compactmods.crafting.CompactCrafting.MOD_ID;
import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.field.MissingFieldsException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class BlockEventHandler {

    @SubscribeEvent
    static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final LivingEntity entity = event.getEntityLiving();
        final BlockRayTraceResult hitVec = event.getHitVec();

        World w = event.getWorld();

        if(w.isClientSide) {
            final BlockPos placedAt = hitVec.getBlockPos().relative(hitVec.getDirection());
            try {
                final boolean allowPlace = FieldHelper.checkBlockPlacement(w, placedAt);
                if (!allowPlace) {
                    event.setCanceled(true);
                }

            } catch (MissingFieldsException e) {
                CompactCrafting.LOGGER.error("Missing the active miniaturization fields capability in the level. Report this!");
            }
        }
    }

    @SubscribeEvent
    static void onBlockPlaced(final BlockEvent.EntityPlaceEvent blockPlaced) {
        blockHandler(blockPlaced);
    }

    @SubscribeEvent
    static void onBlockDestroyed(final BlockEvent.BreakEvent blockDestroyed) {
        blockHandler(blockDestroyed);
    }

    private static void blockHandler(final BlockEvent event) {
        // Check if block is in or around a projector field
        IWorld world = event.getWorld();
        BlockPos pos = event.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        if (world instanceof World) {
            try {
                boolean allowPlace = FieldHelper.checkBlockPlacement((World) world, pos);
                if (!allowPlace) {
                    event.setCanceled(true);
                }
            } catch (MissingFieldsException e) {
                CompactCrafting.LOGGER.error("Missing the active miniaturization fields capability in the level. Report this!");
            }
        }
    }
}
