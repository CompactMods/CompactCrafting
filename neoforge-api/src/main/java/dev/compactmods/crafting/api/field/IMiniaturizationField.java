package dev.compactmods.crafting.api.field;

import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationField {

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

    boolean enabled();

    void tick();
}
