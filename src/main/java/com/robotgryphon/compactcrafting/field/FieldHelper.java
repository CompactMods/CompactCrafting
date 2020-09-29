package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(IWorld world, BlockPos pos, BlockUpdateType type) {
        // We don't care about client worlds RN
        if(world.isRemote())
            return;

        ProjectionFieldSavedData data = ProjectionFieldSavedData.get((ServerWorld) world);
        if(data.ACTIVE_FIELDS.isEmpty())
            return;

        List<BlockPos> projectors = new ArrayList<>();
        int maxDimensions = FieldProjectionSize.maximum().getDimensions();
        for(BlockPos center : data.ACTIVE_FIELDS.keySet()) {
            // FieldProjectorTile mainTile = (FieldProjectorTile) world.getTileEntity(fielf)
            boolean closeEnough = center.withinDistance(pos, maxDimensions);
            if(closeEnough) {
                ProjectorFieldData field = data.ACTIVE_FIELDS.get(center);
                projectors.add(field.mainProjector);
            }
        }

        for (BlockPos p : projectors) {
            FieldProjectorTile tile = (FieldProjectorTile) world.getTileEntity(p);

            CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Not a field projector tile. Somehow.
            if (tile == null) {
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: " + p.getCoordinatesAsString());
                continue;
            }

            tile.handleNearbyBlockUpdate(pos, type);
        }
    }

    public static Stream<AxisAlignedBB> splitIntoLayers(FieldProjectionSize size, AxisAlignedBB full) {

        int s = size.getSize();
        BlockPos bottomCenter = new BlockPos(full.getCenter()).down(s);
        AxisAlignedBB bottomLayerBounds = new AxisAlignedBB(bottomCenter).grow(s, 0, s);

        AxisAlignedBB[] layers = new AxisAlignedBB[size.getDimensions()];
        for(int layer = 0; layer < size.getDimensions(); layer++) {
            AxisAlignedBB layerBounds = bottomLayerBounds.offset(0, layer, 0);
            layers[layer] = layerBounds;
        }

        return Stream.of(layers);
    }
}
