package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(IWorld world, BlockPos pos) {
        // We don't care about client worlds RN
        if(world.isClientSide())
            return;

        ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
        if(data.ACTIVE_FIELDS.isEmpty())
            return;

        List<BlockPos> projectors = new ArrayList<>();
        int maxDimensions = FieldProjectionSize.maximum().getDimensions();
        for(BlockPos center : data.ACTIVE_FIELDS.keySet()) {
            boolean closeEnough = center.closerThan(pos, maxDimensions);
            if(closeEnough) {
                ProjectorFieldData field = data.ACTIVE_FIELDS.get(center);
                projectors.add(field.mainProjector);
            }
        }

        for (BlockPos p : projectors) {
            FieldProjectorTile tile = (FieldProjectorTile) world.getBlockEntity(p);

            // CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Not a field projector tile. Somehow.
            if (tile == null) {
                // toShortString() is client only, so let's just use toString()
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: {}", p);
                continue;
            }

            Optional<FieldProjection> field = tile.getField();

            if(field.isPresent()) {
                AxisAlignedBB fieldBounds = field.get().getBounds();

                // Is the block update INSIDE the current field?
                boolean blockInField = fieldBounds
                        .contains(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);

                if(!blockInField)
                    continue;;

                // Schedule an update tick for half a second out, handles blocks breaking better
                world
                    .getBlockTicks()
                    .scheduleTick(p, Registration.FIELD_PROJECTOR_BLOCK.get(), 10, TickPriority.NORMAL);
            }
        }
    }


}