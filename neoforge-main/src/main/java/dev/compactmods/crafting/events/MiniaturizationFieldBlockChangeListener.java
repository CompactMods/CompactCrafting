package dev.compactmods.crafting.events;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public record MiniaturizationFieldBlockChangeListener(MiniaturizationFieldLocation fieldLocation) implements GameEventListener {
    @Override
    public PositionSource getListenerSource() {
        return new BlockPositionSource(MathUtil.toBlockPosition(fieldLocation.center()));
    }

    @Override
    public int getListenerRadius() {
        return MiniaturizationFieldSize.maximum().getRadius() + 1;
    }

    @Override
    public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 sourcePosition) {
        return false;
    }
}
