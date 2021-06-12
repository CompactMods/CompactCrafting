package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.projector.tile.DummyFieldProjectorTile;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import com.robotgryphon.compactcrafting.projector.tile.MainFieldProjectorTile;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(IWorld world, BlockPos pos) {
        // We don't care about client worlds RN
        if(world.isClientSide())
            return;

        int maxDimensions = FieldProjectionSize.maximum().getDimensions();
        AxisAlignedBB searchArea = new AxisAlignedBB(pos, pos).inflate(maxDimensions);

        BlockPos[] nearbyProjectors = BlockPos.betweenClosedStream(searchArea)
                .filter(possProjector -> world.getBlockState(possProjector).getBlock() instanceof FieldProjectorBlock)
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        CompactCrafting.LOGGER.debug("Found {} nearby projectors.", nearbyProjectors.length);

        for (BlockPos p : nearbyProjectors) {
            FieldProjectorTile tile = (FieldProjectorTile) world.getBlockEntity(p);
            if(tile instanceof DummyFieldProjectorTile)
                continue;

            MainFieldProjectorTile main = (MainFieldProjectorTile) tile;

            // CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Not a field projector tile. Somehow.
            if (tile == null) {
                // toShortString() is client only, so let's just use toString()
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: {}", p);
                continue;
            }

            LazyOptional<IMiniaturizationField> field = main.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD, null);
            field.ifPresent(f -> {
                AxisAlignedBB fieldBounds = f.getBounds();

                // Is the block update INSIDE the current field?
                boolean blockInField = fieldBounds
                        .contains(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);

                if(!blockInField)
                    return;

                // Schedule an update tick for half a second out, handles blocks breaking better
                world
                        .getBlockTicks()
                        .scheduleTick(p, Registration.FIELD_PROJECTOR_BLOCK.get(), 10, TickPriority.NORMAL);
            });
        }
    }


}