package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorTile;
import com.robotgryphon.compactcrafting.core.BlockUpdateType;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.world.ProjectionFieldSavedData;
import com.robotgryphon.compactcrafting.world.ProjectorFieldData;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
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

            // CompactCrafting.LOGGER.debug("Got a block placed near a projector: " + p.getCoordinatesAsString());

            // Not a field projector tile. Somehow.
            if (tile == null) {
                CompactCrafting.LOGGER.warn("Warning: Got a projector block but there was no field projector TE on the same position. Position: " + p.getCoordinatesAsString());
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
                    .getPendingBlockTicks()
                    .scheduleTick(p, Registration.FIELD_PROJECTOR_BLOCK.get(), 10, TickPriority.NORMAL);
            }
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

    /**
     * Converts a layer of a field into its relative recipe component definition.
     *
     * @param world
     * @param recipe
     * @param fieldSize
     * @param layer
     * @return
     */
    public static Map<BlockPos, String> remapLayerToRecipe(IWorldReader world, MiniaturizationRecipe recipe, FieldProjectionSize fieldSize, AxisAlignedBB layer) {
        Map<BlockPos, String> relativeMap = new HashMap<>();

        BlockPos[] filled = BlockPos.getAllInBox(layer)
                .filter(pos -> !world.isAirBlock(pos))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        for(BlockPos pos : filled) {
            BlockState state = world.getBlockState(pos);
            FluidState fluid = world.getFluidState(pos);

            // TODO: Fluid crafting! :D

            Optional<String> recipeComponentKey = recipe.getRecipeComponentKey(state);
            if(recipeComponentKey.isPresent())
            {
                // Get relative position in layer and add to map
                BlockPos normalized = RecipeHelper.normalizeLayerPosition(layer, pos);
                relativeMap.put(normalized, recipeComponentKey.get());
            }
        }

        return relativeMap;
    }


}