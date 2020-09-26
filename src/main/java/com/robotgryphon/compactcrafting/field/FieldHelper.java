package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.blockstateprovider.BlockStateProviderType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(IWorld world, BlockPos pos) {
        // Scan all blocks in the maximum field projection range (trying to find projectors)
        AxisAlignedBB scanBlocks = new AxisAlignedBB(pos).grow(FieldProjectionSize.maximum().getOffset());

        Set<BlockPos> projectors = BlockPos.getAllInBox(scanBlocks)
                .filter(p -> !world.isAirBlock(p))
                .filter(p -> {
                    BlockState bs = world.getBlockState(p);
                    return bs.getBlock() instanceof FieldProjectorBlock;
                })
                .collect(Collectors.toSet());

        if(!projectors.isEmpty()) {
            // We have projectors in the surrounding area, notify them to check their fields
            for(BlockPos projectorPos : projectors) {
                BlockState state = world.getBlockState(projectorPos);
                FieldProjectorTile tile = (FieldProjectorTile) world.getTileEntity(projectorPos);

                // Not a field projector tile. Somehow.
                if(tile == null) {
                    CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: " + projectorPos.getCoordinatesAsString());
                    continue;
                }

                tile.handleNearbyBlockUpdate(pos, BlockUpdateType.PLACE);
            }
        }
    }
}
