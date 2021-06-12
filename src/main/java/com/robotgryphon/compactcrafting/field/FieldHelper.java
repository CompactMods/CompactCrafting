package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(World world, BlockPos pos) {
        // We don't care about client worlds RN
        if (world.isClientSide())
            return;

        ServerWorld sWorld = (ServerWorld) world;

        int maxDimensions = FieldProjectionSize.maximum().getDimensions();
        AxisAlignedBB searchArea = new AxisAlignedBB(pos, pos).inflate(maxDimensions);

        BlockPos[] nearbyProjectors = BlockPos.betweenClosedStream(searchArea)
                .filter(possProjector -> world.getBlockState(possProjector).getBlock() instanceof FieldProjectorBlock)
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        CompactCrafting.LOGGER.debug("Found {} nearby projectors.", nearbyProjectors.length);

        if (nearbyProjectors.length > 0) {
            world.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.getFields()
                            .filter(field -> {
                                BlockPos fieldCenter = field.getCenterPosition();
                                return searchArea.contains(
                                        fieldCenter.getX(),
                                        fieldCenter.getY(),
                                        fieldCenter.getZ()
                                );
                            }).forEach(field -> field.doRecipeScan(world));
                });
        }
    }


}