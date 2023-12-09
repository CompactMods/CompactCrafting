package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.field.MissingFieldsException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import static dev.compactmods.crafting.CompactCrafting.MOD_ID;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class BlockEventHandler {

//    @SubscribeEvent
    static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final LivingEntity entity = event.getEntity();
        final BlockHitResult hitVec = event.getHitVec();

        Level w = event.getLevel();

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
        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        // Send the event position over to the field helper, so any nearby projectors can be notified
        if (world instanceof Level) {
            try {
                boolean allowPlace = FieldHelper.checkBlockPlacement((Level) world, pos);
                if (!allowPlace && event instanceof ICancellableEvent cancel)
                    cancel.setCanceled(true);

            } catch (MissingFieldsException e) {
                CompactCrafting.LOGGER.error("Missing the active miniaturization fields capability in the level. Report this!");
            }
        }
    }
}
