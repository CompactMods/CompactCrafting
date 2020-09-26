package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(IWorld world, BlockPos pos) {
        // Scan all blocks in the maximum field projection range (trying to find projectors)
        AxisAlignedBB scanBlocks = new AxisAlignedBB(pos).grow(FieldProjectionSize.maximum().getProjectorDistance() + 1);

        BlockPos[] potentials = BlockPos.getAllInBox(scanBlocks)
                .map(BlockPos::toImmutable)
                .filter(p -> !world.isAirBlock(p))
                .filter(p -> world.getBlockState(p).getBlock() instanceof FieldProjectorBlock)
                .toArray(BlockPos[]::new);

        for (BlockPos p : potentials) {
            BlockState bs = world.getBlockState(p);
            FieldProjectorTile tile = (FieldProjectorTile) world.getTileEntity(p);

            CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Not a field projector tile. Somehow.
            if (tile == null) {
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: " + p.getCoordinatesAsString());
                continue;
            }

            tile.handleNearbyBlockUpdate(p, BlockUpdateType.PLACE);
        }
    }

    public static AxisAlignedBB[] splitIntoLayers(FieldProjectionSize size, AxisAlignedBB full) {

        int s = size.getSize();
        BlockPos bottomCenter = new BlockPos(full.getCenter()).down(s);
        AxisAlignedBB bottomLayerBounds = new AxisAlignedBB(bottomCenter).grow(s, 0, s);

        AxisAlignedBB[] layers = new AxisAlignedBB[size.getDimensions()];
        for(int layer = 0; layer <= size.getDimensions(); layer++) {
            AxisAlignedBB layerBounds = bottomLayerBounds.offset(0, layer, 0);
            layers[layer] = layerBounds;
        }

        return layers;
    }
}
