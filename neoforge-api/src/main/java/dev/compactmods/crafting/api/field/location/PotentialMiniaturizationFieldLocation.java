package dev.compactmods.crafting.api.field.location;

import dev.compactmods.crafting.api.projector.placement.InvalidProjectorPlacement;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;

import java.util.Set;

public record PotentialMiniaturizationFieldLocation(
        MiniaturizationFieldLocation fieldLocation,
        Set<ProjectorPlacement> validProjectors,
        Set<InvalidProjectorPlacement> invalidProjectors
) {
}
