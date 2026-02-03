package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import net.minecraft.world.level.Level;

public interface IMiniaturizationField {

    Level level();

    MiniaturizationFieldLocation location();

    FieldProjectorPlacements getProjectors();

    boolean isAreaLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    void tick();
}
