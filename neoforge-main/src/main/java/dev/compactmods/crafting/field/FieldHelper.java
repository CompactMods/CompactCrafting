package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.projector.FieldProjectorPredicates;
import dev.compactmods.crafting.api.projector.FieldProjectorTags;
import dev.compactmods.crafting.core.CCAttachments;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Provides utilities to help with projector projectors management.
 */
public abstract class FieldHelper {
    public static boolean checkBlockPlacement(Level level, BlockPos pos) throws MissingFieldsException {
        int maxDimensions = MiniaturizationFieldSize.maximum().getDimensions();
        AABB searchArea = AABB.encapsulatingFullBlocks(pos, pos).inflate(maxDimensions + 3);

        BlockPos[] nearbyProjectors = BlockPos.betweenClosedStream(searchArea)
                .filter(possProjector -> level.getBlockState(possProjector)
                        .is(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK, FieldProjectorPredicates.IS_ACTIVE))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        if (ServerConfig.FIELD_BLOCK_CHANGES.get())
            CompactCrafting.LOGGER.debug("Found {} nearby projectors near {}.", nearbyProjectors.length, pos);

        final Vec3 centerBlockChanged = Vec3.atCenterOf(pos);
        if (nearbyProjectors.length > 0) {
            final var fields = level.getData(CCAttachments.ACTIVE_FIELDS);

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