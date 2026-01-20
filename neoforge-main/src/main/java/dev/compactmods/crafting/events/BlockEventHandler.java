package dev.compactmods.crafting.events;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.field.MissingFieldsException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class BlockEventHandler {

    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final LivingEntity entity = event.getEntity();
        final BlockHitResult hitVec = event.getHitVec();

        Level w = event.getLevel();

        if(w.isClientSide()) {
            final BlockPos placedAt = hitVec.getBlockPos().relative(hitVec.getDirection());
            try {
                final boolean allowPlace = FieldHelper.checkBlockPlacement(w, placedAt);
                if (!allowPlace) {
                    event.setCanceled(true);
                }

            } catch (MissingFieldsException e) {
                CompactCrafting.LOGGER.error("Missing the active miniaturization fields capability in the level. Report this!", e);
            }
        }
    }

    static void onBlockPlaced(final BlockEvent.EntityPlaceEvent blockPlaced) {
        blockHandler(blockPlaced);
    }

    static void onBlockDestroyed(final BlockEvent.BreakEvent blockDestroyed) {
        blockHandler(blockDestroyed);
    }

    private static void blockHandler(final BlockEvent event) {
        // Check if block is in or around a projector projectors
        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        // Send the event position over to the projectors helper, so any nearby projectors can be notified
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
