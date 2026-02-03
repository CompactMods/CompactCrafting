package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.field.location.PotentialMiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.InvalidProjectorPlacement;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Contains utility methods for working with a set of projectors in a given space.
public abstract class ProjectorHelper {

    /// Given a starting point for a field, do any of the field sizes contain more than one projector
    /// targeting the given size?
    public static Stream<PotentialMiniaturizationFieldLocation> findPotentialFields(Level level, ProjectorPlacement starting) {
        final var s = Stream.<PotentialMiniaturizationFieldLocation>builder();
        for(var fieldSize : MiniaturizationFieldSize.values()) {
            final var fieldLocation = MiniaturizationFieldLocation.compute(level.dimension(), fieldSize, starting);
            final var projectors = fieldLocation.projectors();

            final var valid = projectors.validProjectors(level)
                    .collect(Collectors.toUnmodifiableSet());

            final var invalid = projectors.invalidProjectors(level)
                    .filter(ip -> !ip.reason().equals(InvalidProjectorPlacement.Reason.Missing))
                    .collect(Collectors.toUnmodifiableSet());

            s.add(new PotentialMiniaturizationFieldLocation(fieldLocation, valid, invalid));
        }

        return s.build();
    }
}
