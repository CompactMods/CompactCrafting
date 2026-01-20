package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.FieldProjectorProperties;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import dev.compactmods.crafting.core.CCBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;

public class FieldProjectorItem extends Item {

    public FieldProjectorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult placeResult = this.place(new BlockPlaceContext(context));
        return !placeResult.consumesAction() && context.getItemInHand().has(DataComponents.CONSUMABLE)
                ? super.use(context.getLevel(), context.getPlayer(), context.getHand())
                : placeResult;
    }

    public InteractionResult place(BlockPlaceContext placeContext) {
        if (!placeContext.canPlace())
            return InteractionResult.FAIL;

        final var pos = placeContext.getClickedPos();
        final var level = placeContext.getLevel();
        final var player = placeContext.getPlayer();
        final var itemStack = placeContext.getItemInHand();

        BlockState placementState = this.getPlacementState(placeContext);

        final var placed = placeContext.getLevel()
                .setBlock(placeContext.getClickedPos(), placementState, Block.UPDATE_ALL_IMMEDIATE);

        if (!placed)
            return InteractionResult.FAIL;

        BlockState placedState = level.getBlockState(pos);

        placedState.getBlock().setPlacedBy(level, pos, placedState, player, itemStack);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, itemStack);
        }

        SoundType soundType = placedState.getSoundType(level, pos, player);
        level.playSound(
                player, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F
        );

        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, placedState));
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @NonNull
    private BlockState getPlacementState(BlockPlaceContext context) {
        final var level = context.getLevel();

        ProjectorPlacement placement = ProjectorPlacement.compute(context);

        return placement.getPossibleFields()
                .filter(fieldLocation -> fieldLocation.projectors()
                        .invalidProjectors(level)
                        .findAny()
                        .isEmpty())
                .findFirst()
                .map(fieldLocation -> activatingFieldState(context, fieldLocation))
                .orElse(inactiveProjectorState(context))
                .setValue(BlockStateProperties.HORIZONTAL_FACING, placement.facing());
    }

    @NonNull
    private static BlockState activatingFieldState(BlockPlaceContext placeContext, MiniaturizationFieldLocation fieldLocation) {
        final var activeProjectorBlock = CCBlocks.FIELD_PROJECTOR_BLOCK.get();

        final var baseActive = activeProjectorBlock.getStateForPlacement(placeContext);
        if (baseActive != null)
            return baseActive.setValue(FieldProjectorProperties.SIZE, fieldLocation.size());

        return activeProjectorBlock.defaultBlockState();
    }

    @NonNull
    private static BlockState inactiveProjectorState(BlockPlaceContext placeContext) {
        final var offlineFieldProjectorBlock = CCBlocks.INACTIVE_FIELD_PROJECTOR_BLOCK.get();
        final var offlineBaseState = offlineFieldProjectorBlock.getStateForPlacement(placeContext);

        if (offlineBaseState != null)
            return offlineBaseState;

        return offlineFieldProjectorBlock.defaultBlockState();
    }
}
