package com.robotgryphon.compactcrafting.command;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.MiniaturizationFieldBlockData;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import com.robotgryphon.compactcrafting.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import com.robotgryphon.compactcrafting.util.GuiUtil;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecipeCopyCommand {
    private static final DynamicCommandExceptionType INVALID_COMPONENT_KEY = new DynamicCommandExceptionType(key ->
            new TranslationTextComponent("commands.compactcrafting.recipe.copy.invalid_component_key", key));

    private RecipeCopyCommand() {}

    public static void register(LiteralArgumentBuilder<CommandSource> base) {
        base.then(Commands.literal("recipe")
                .then(Commands.literal("copy")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(RecipeCopyCommand::run)));
    }

    private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        CommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrException();

        BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) player.pick(32, 1.0F, false);

        TileEntity baseTile = player.level.getBlockEntity(rayTraceResult.getBlockPos());
        if (rayTraceResult.getType() == RayTraceResult.Type.MISS || !(baseTile instanceof FieldProjectorTile)) {
            source.sendFailure(new TranslationTextComponent("commands.compactcrafting.recipe.copy.no_projector"));
            return 0;
        }

        FieldProjectorTile projectorTile = (FieldProjectorTile) baseTile;
        FieldProjection field = projectorTile.getField().orElse(null);
        if (field == null) {
            source.sendFailure(new TranslationTextComponent("commands.compactcrafting.recipe.copy.no_field"));
            return 0;
        }

        MiniaturizationFieldBlockData fieldBlockData = MiniaturizationFieldBlockData.getFromField(player.level, field.getBounds());

        AxisAlignedBB filledFieldBounds = fieldBlockData.getFilledBounds();
        ImmutableList<BlockPos> filledPos = fieldBlockData.getFilledBlocks();

        if (filledPos.isEmpty()) {
            source.sendFailure(new TranslationTextComponent("commands.compactcrafting.recipe.copy.empty_field"));
            return 0;
        }

        // Maps world pos -> relative pos
        Map<BlockPos, BlockPos> relativeMap = BlockSpaceUtil.mapNormalizedLayerPositions(filledFieldBounds, filledPos);

        // Collect some stats
        IntSummaryStatistics[] stats = BlockSpaceUtil.getBlockPosStats(relativeMap.values());
        BlockPos minPos = BlockSpaceUtil.getMinBlockPos(stats);
        // BlockPos maxPos = new BlockPos(statsX.getMax(), statsY.getMax(), statsZ.getMax()).subtract(minPos);

        // Subtract the smallest position from every other
        relativeMap = relativeMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().subtract(minPos)));
        // Maps relative pos -> world blockstate
        Map<BlockPos, BlockState> posStateMap = filledPos.stream()
                .collect(Collectors.toMap(relativeMap::get, p -> player.level.getBlockState(p)));
        // Maps relative pos -> component key
        Map<BlockPos, String> componentPosMap = new HashMap<>();
        // Maps component key -> world blockstate
        BiMap<String, BlockState> componentStateMap = HashBiMap.create();
        for (Map.Entry<BlockPos, BlockState> entry : posStateMap.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (componentStateMap.containsValue(state)) {
                componentPosMap.put(pos, componentStateMap.inverse().get(state));
                continue;
            }

            ResourceLocation location = state.getBlock().getRegistryName();
            if (location == null)
                continue;
            String path = location.getPath().toUpperCase(Locale.ROOT);
            String componentKey = "";
            int index = 0;
            int targetLength = 1;
            do {
                if (index + targetLength > path.length()) {
                    if (targetLength >= path.length()) {
                        throw INVALID_COMPONENT_KEY.create(location);
                    }
                    targetLength++;
                    index = 0;
                }
                componentKey = path.substring(index, index + targetLength);
                index++;
                // Don't let _ be a component key as we need that for air blocks
            } while ("_".equals(componentKey) || componentStateMap.containsKey(componentKey));

            componentStateMap.put(componentKey, state);
            componentPosMap.put(pos, componentKey);
        }
        Map<String, RecipeBlockStateComponent> componentWrappedMap = componentStateMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new RecipeBlockStateComponent(e.getValue())));

        int maxY = stats[1].getMax();
        AxisAlignedBB relativeBounds = BlockSpaceUtil.getBoundsForBlocks(relativeMap.values());
        relativeBounds = new AxisAlignedBB(0, 0, 0, relativeBounds.maxX, relativeBounds.maxY, relativeBounds.maxZ);
        List<IRecipeLayer> layers = getLayers(componentPosMap, maxY, relativeBounds, false);
        MiniaturizationRecipe recipe = new MiniaturizationRecipe(null, (int) Math.max(relativeBounds.maxX, relativeBounds.maxZ), layers, componentWrappedMap);
        GuiUtil.openMiniaturizationGui(player, recipe, true);

        return 1;
    }

    private static List<IRecipeLayer> getLayers(Map<BlockPos, String> componentPosMap, int maxY, AxisAlignedBB relativeBounds, boolean reverseMatch) {
        List<IRecipeLayer> layers = new ArrayList<>();
        for (int y = 0; y <= maxY; y++) {
            int finalY = y;
            Map<BlockPos, String> filtered = componentPosMap.entrySet().stream()
                    .filter(e -> e.getKey().getY() == finalY)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (filtered.isEmpty())
                continue;
            IRecipeLayer layer = getMatchedLayer(filtered, relativeBounds, reverseMatch);
            if (layer == null)
                continue;
            layers.add(layer);
        }
        // If not square and all dynamic layers, this is impossible so we have to match in reverse to get all rigid layers
        if (!reverseMatch && relativeBounds.getXsize() != relativeBounds.getZsize() && layers.stream().allMatch(IDynamicRecipeLayer.class::isInstance)) {
            return getLayers(componentPosMap, maxY, relativeBounds, true);
        }
        return layers;
    }

    private static IRecipeLayer getMatchedLayer(Map<BlockPos, String> compMap, AxisAlignedBB recipeDimensions, boolean reverseMatch) {
        return Registration.getRecipeLayerMatchers().getValues().stream()
                .sorted(reverseMatch ? Comparator.reverseOrder() : Comparator.naturalOrder())
                .map(matcher -> matcher.getMatch(compMap, recipeDimensions))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}
