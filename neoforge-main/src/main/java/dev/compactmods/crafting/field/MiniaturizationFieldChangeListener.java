package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.recipe.CCTags;
import dev.compactmods.crafting.field.impl.MiniaturizationField;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

public record MiniaturizationFieldChangeListener(MiniaturizationField field) implements GameEventListener {
    @Override
    public PositionSource getListenerSource() {
        return new BlockPositionSource(field.location().centerBlock());
    }

    @Override
    public int getListenerRadius() {
        return field.location().size().getRadius() + 1;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 sourcePosition) {
        if(event.is(CCTags.FIELD_CHANGE_EVENTS)) {
            field.fieldContentsChanged();
            return true;
        }

        return false;
    }
}
