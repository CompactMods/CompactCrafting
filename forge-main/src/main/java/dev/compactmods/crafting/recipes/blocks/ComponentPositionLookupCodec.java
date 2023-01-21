package dev.compactmods.crafting.recipes.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.RecipeHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ComponentPositionLookupCodec implements PrimitiveCodec<ComponentPositionLookup> {
    @Override
    public <T> DataResult<ComponentPositionLookup> read(DynamicOps<T> ops, T input) {
        return Codec.STRING.listOf().listOf().decode(ops, input).flatMap(s -> {
            List<List<String>> layerList = s.getFirst();

            ComponentPositionLookup lookup = new ComponentPositionLookup();

            int zSize = layerList.size();

            String[][] mappedToArray = new String[zSize][];

            for (int z = 0; z < zSize; z++) {
                List<String> layerComponents = layerList.get(z);
                String[] xValues = new String[layerComponents.size()];
                for (int x = 0; x < layerComponents.size(); x++) {
                    xValues[x] = layerComponents.get(x);
                }

                mappedToArray[z] = xValues;
            }

            lookup.setFootprint(mappedToArray[0].length, zSize);
            lookup.components.putAll(RecipeHelper.convertMultiArrayToMap(mappedToArray));
            lookup.rebuildComponentTotals();

            return DataResult.success(lookup);
        });
    }

    @Override
    public <T> T write(DynamicOps<T> ops, ComponentPositionLookup lookup) {
        final var footprint = lookup.footprint();
        AABB boundsForBlocks = AABB.of(footprint);
        final String[][] map = RecipeHelper.generateArrayFromBounds(boundsForBlocks);

        BlockSpaceUtil.getBlocksIn(boundsForBlocks)
                .map(pos -> Pair.of(pos.immutable(), lookup.getRequiredComponentKeyForPosition(pos).orElse("-")))
                .forEach(pair -> {
                    final BlockPos p = pair.getFirst();
                    try {
                        map[p.getX()][p.getZ()] = pair.getSecond();
                    } catch (ArrayIndexOutOfBoundsException aio) {
                        CompactCrafting.RECIPE_LOGGER.error(aio);
                    }
                });

        List<List<String>> fin = Arrays.stream(map).map(ImmutableList::copyOf).collect(Collectors.toList());

        DataResult<T> encoded = Codec.STRING.listOf().listOf().encode(fin, ops, ops.empty());

        return encoded
                .resultOrPartial(err -> CompactCrafting.LOGGER.error("Failed to encode layer component position lookup: {}", err))
                .get();
    }
}