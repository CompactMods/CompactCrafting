package dev.compactmods.crafting.events;

import dev.compactmods.crafting.CompactCrafting;
import static dev.compactmods.crafting.CompactCrafting.MOD_ID;
import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.field.MissingFieldsException;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import dev.compactmods.crafting.field.FieldHelper;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class BlockEventHandler {

    @SubscribeEvent
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
                if (!allowPlace) {
                    event.setCanceled(true);
                }
            } catch (MissingFieldsException e) {
                CompactCrafting.LOGGER.error("Missing the active miniaturization fields capability in the level. Report this!");
            }
        }
    }
}
