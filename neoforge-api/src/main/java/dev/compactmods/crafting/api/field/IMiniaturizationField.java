package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationField {

    default void dispose() {}

    AABB getBounds();

    MiniaturizationFieldLocation location();

    int getProgress();

    FieldProjectorPlacements getProjectors();

    EnumCraftingState getCraftingState();

    boolean isAreaLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    default void handleDestabilize() {}

    void checkRedstone();

    boolean enabled();
}
