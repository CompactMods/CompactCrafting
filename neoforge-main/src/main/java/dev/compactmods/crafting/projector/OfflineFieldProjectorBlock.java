package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.projector.ProjectorHelper;
import dev.compactmods.crafting.api.projector.world.ProjectorBlock;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Comparator;

public class OfflineFieldProjectorBlock extends FieldProjectorBlock implements ProjectorBlock {
    public OfflineFieldProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            final var initial = ProjectorBlock.placement(pos, state);
            player.removeData(FieldProjectorsCommon.PLACEMENT_HELPERS);
            final var helpers = new ObjectArrayList<GhostProjectorPlacementRenderer>();

            // TODO: Highlight invalid projectors with a red box
            final var potentials = ProjectorHelper.findPotentialFields(level, initial)
                    .filter(potential -> potential.invalidProjectors().isEmpty())
                    .sorted(Comparator.comparingInt(p -> p.validProjectors().size()))
                    .toList()
                    .reversed();

            final var bestMatch = potentials.stream()
                    .filter(p -> p.validProjectors().size() > 1)
                    .findFirst();

            if(bestMatch.isPresent()) {
                final var best = bestMatch.get();
                final var helper = new GhostProjectorPlacementRenderer(best.fieldLocation());
                helpers.add(helper);
            } else {
                potentials.forEach(field -> {
                    final var helper = new GhostProjectorPlacementRenderer(field.fieldLocation());
                    helpers.add(helper);
                });
            }

            player.setData(FieldProjectorsCommon.PLACEMENT_TIMER, ClientConfig.placementTime);
            player.setData(FieldProjectorsCommon.PLACEMENT_HELPERS, helpers);
        }

        return InteractionResult.SUCCESS;
    }
}
