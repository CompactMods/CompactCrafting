package dev.compactmods.crafting.api.projector;

import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Predicate;

public abstract class FieldProjectorPredicates {

    public static final Predicate<BlockBehaviour.BlockStateBase> IS_PROJECTOR =
            state -> state.is(FieldProjectorTags.PROJECTOR_BLOCK);

    public static final Predicate<BlockBehaviour.BlockStateBase> IS_ACTIVE =
            state -> state.is(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK);

    /// The projector state does not indicate it is part of an active Miniaturization field.
    public static final Predicate<BlockBehaviour.BlockStateBase> IS_INACTIVE =
            state -> state.is(FieldProjectorTags.INACTIVE_PROJECTOR_BLOCK);

}
