package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides utilities to help with projector field management.
 */
public class FieldHelper {
    private FieldHelper() {}

    public static void checkBlockPlacement(IWorld world, BlockPos pos, BlockUpdateType type) {
        // We don't care about client worlds RN
        if (world.isClientSide())
            return;

        ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
        Map<BlockPos, ProjectorFieldData> activeFields = data.getActiveFields();
        if (activeFields.isEmpty())
            return;

        List<BlockPos> projectors = new ArrayList<>();
        int maxDimensions = FieldProjectionSize.getMaximumSize().getDimensions();
        for (Map.Entry<BlockPos, ProjectorFieldData> entry : activeFields.entrySet()) {
            BlockPos center = entry.getKey();
            ProjectorFieldData field = entry.getValue();
            boolean closeEnough = center.closerThan(pos, maxDimensions);
            if (closeEnough)
                projectors.add(field.mainProjector);
        }

        for (BlockPos p : projectors) {
            FieldProjectorTile tile = (FieldProjectorTile) world.getBlockEntity(p);

            // CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Somehow was a null field projector tile
            if (tile == null) {
                // toShortString() is client only, so let's just use toString()
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: {}", p);
                continue;
            }

            if (!tile.isMainProjector())
                continue;

            Optional<FieldProjection> field = tile.getField();

            if (field.isPresent()) {
                AxisAlignedBB fieldBounds = field.get().getBounds();

                // Is the block update INSIDE the current field?
                boolean blockInField = fieldBounds.contains(Vector3d.atCenterOf(pos));

                if (!blockInField)
                    continue;

                // Schedule an update tick for half a second out, handles blocks breaking better
                world.getBlockTicks()
                        .scheduleTick(p, Registration.FIELD_PROJECTOR_BLOCK.get(), 10, TickPriority.NORMAL);
            }
        }
    }
}