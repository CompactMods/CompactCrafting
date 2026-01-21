package dev.compactmods.crafting.api.projector.capability;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;

import java.util.Optional;

/// An interface used to mark blocks as field projectors.
/// Intended to be used via NeoForge's capability system.
public interface FieldProjectorController {

    ProjectorPlacement placement();

    Optional<MiniaturizationFieldLocation> fieldLocation();

    boolean isActive();

    boolean activate();

    boolean deactivate();
}
