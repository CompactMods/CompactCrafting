package dev.compactmods.crafting.field;

import java.util.Optional;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Provides utilities to help with projector field management.
 */
public abstract class FieldHelper {
    public static boolean checkBlockPlacement(Level level, BlockPos pos) throws MissingFieldsException {
        int maxDimensions = MiniaturizationFieldSize.maximum().getDimensions();
        AABB searchArea = new AABB(pos, pos).inflate(maxDimensions + 3);

        BlockPos[] nearbyProjectors = BlockPos.betweenClosedStream(searchArea)
                .filter(possProjector -> level.getBlockState(possProjector).getBlock() instanceof FieldProjectorBlock)
                .filter(possProjector -> FieldProjectorBlock.isActive(level.getBlockState(possProjector)))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        if (ServerConfig.FIELD_BLOCK_CHANGES.get())
            CompactCrafting.LOGGER.debug("Found {} nearby projectors near {}.", nearbyProjectors.length, pos);

        final Vec3 centerBlockChanged = Vec3.atCenterOf(pos);
        if (nearbyProjectors.length > 0) {
            final IActiveWorldFields fields = level.getCapability(CCCapabilities.FIELDS)
                    .orElseThrow(() -> new MissingFieldsException("Could not fetch fields off level: " + level.dimension()));

            final Optional<IMiniaturizationField> affectedField = fields.getFields()
                    .filter(field -> field.getBounds().contains(centerBlockChanged))
                    .findFirst();

            return affectedField.map(field -> {
                if (field.getCraftingState() == EnumCraftingState.CRAFTING)
                    return false;

                field.fieldContentsChanged();
                return true;
            }).orElse(true);
        }

        return true;
    }
}