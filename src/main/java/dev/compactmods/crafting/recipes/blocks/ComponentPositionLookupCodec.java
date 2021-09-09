package dev.compactmods.crafting.recipes.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.RecipeHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;

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

            lookup.components.putAll(RecipeHelper.convertMultiArrayToMap(mappedToArray));

            return DataResult.success(lookup);
        });
    }

    @Override
    public <T> T write(DynamicOps<T> ops, ComponentPositionLookup value) {
        AxisAlignedBB boundsForBlocks = BlockSpaceUtil.getBoundsForBlocks(value.getAllPositions());

        HashMap<Integer, List<String>> revMap = new HashMap<>((int) boundsForBlocks.getXsize());
        for (int x = 0; x < boundsForBlocks.getXsize(); x++)
            revMap.putIfAbsent(x, new ArrayList<>());

        value.components.forEach((pos, comp) -> {
            List<String> xList = revMap.get(pos.getX());
            xList.add(pos.getZ(), comp);
        });

        List<List<String>> fin = new ArrayList<>(revMap.size());
        revMap.forEach(fin::add);

        DataResult<T> encoded = Codec.STRING.listOf().listOf().encode(fin, ops, ops.empty());

        return encoded
                .resultOrPartial(err -> CompactCrafting.LOGGER.error("Failed to encode layer component position lookup: {}", err))
                .get();
    }
}
