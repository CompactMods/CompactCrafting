package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.server.ServerConfig;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static void checkBlockPlacement(World level, BlockPos pos) {
        // We don't care about client worlds RN
        if (level.isClientSide())
            return;

        int maxDimensions = FieldProjectionSize.maximum().getDimensions();
        AxisAlignedBB searchArea = new AxisAlignedBB(pos, pos).inflate(maxDimensions);

        BlockPos[] nearbyProjectors = BlockPos.betweenClosedStream(searchArea)
                .filter(possProjector -> level.getBlockState(possProjector).getBlock() instanceof FieldProjectorBlock)
                .filter(possProjector -> FieldProjectorBlock.isActive(level.getBlockState(possProjector)))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        if(ServerConfig.FIELD_BLOCK_CHANGES.get())
            CompactCrafting.LOGGER.debug("Found {} nearby projectors near {}.", nearbyProjectors.length, pos);

        final Vector3d centerBlockChanged = Vector3d.atCenterOf(pos);
        if (nearbyProjectors.length > 0) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.getFields()
                            .filter(field -> field.getBounds().contains(centerBlockChanged))
                            .forEach(IMiniaturizationField::markFieldChanged);
                });
        }
    }


}