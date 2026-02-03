package dev.compactmods.crafting.api.projector.placement;

import dev.compactmods.crafting.api.capability.FieldProjectorCapabilities;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record FieldProjectorPlacements(MiniaturizationFieldLocation fieldLocation, Set<ProjectorPlacement> locations) {

    public static FieldProjectorPlacements compute(MiniaturizationFieldLocation field) {
        final var locations = Direction.Plane.HORIZONTAL.stream()
                .map(hor -> ProjectorPlacement.compute(field, hor))
                .collect(Collectors.toUnmodifiableSet());

        return new FieldProjectorPlacements(field, locations);
    }

    /// Gets the placements that have a projector in it (does not check validity, just that a projector exists)
    public Stream<ProjectorPlacement> placedProjectors(LevelReader level) {
        return locations().stream().filter(placement -> placement.isProjector(level));
    }

    /// Given a level, reads information about the potential projector locations
    /// and returns information about valid ones (projectors targeting this field's center)
    public Stream<ProjectorPlacement> activeProjectors(LevelReader level) {
        return placedProjectors(level).filter(p -> p.isActive(level));
    }

    public Stream<ProjectorPlacement> validProjectors(LevelReader level) {
        final var placedProjectors = placedProjectors(level).toList();

        Set<ProjectorPlacement> correct = new ObjectOpenHashSet<>();
        for (var direction : Direction.Plane.HORIZONTAL) {

            final var correctPlacement = get(direction);

            final var placed = placedProjectors.stream()
                    .filter(p -> p.facing().equals(direction))
                    .findFirst();

            if (placed.isPresent() && placed.get().facing().equals(correctPlacement.facing()))
                correct.add(correctPlacement);
        }

        return correct.stream();
    }

    /// Given a level, reads information about the potential projector locations
    /// and returns information about missing or invalid projectors
    public Stream<InvalidProjectorPlacement> invalidProjectors(LevelReader level) {

        final var placedProjectors = placedProjectors(level).toList();

        Set<InvalidProjectorPlacement> invalids = new ObjectOpenHashSet<>();
        for (var direction : Direction.Plane.HORIZONTAL) {

            final var correctPlacement = get(direction);

            final var placed = placedProjectors.stream()
                    .filter(p -> p.facing().equals(direction))
                    .findFirst();

            if (placed.isEmpty()) {
                invalids.add(new InvalidProjectorPlacement(correctPlacement, InvalidProjectorPlacement.Reason.Missing));
                continue;
            }

            final var placed2 = placed.get();
            if (!placed2.facing().equals(correctPlacement.facing())) {
                invalids.add(new InvalidProjectorPlacement(correctPlacement, InvalidProjectorPlacement.Reason.NotTargetingField));
            }
        }

        return invalids.stream();
    }

    /// Fetches the location of a projector facing a side of the field.
    public ProjectorPlacement get(Direction direction) {
        if (direction.getAxis().isHorizontal()) {
            for (var location : locations) {
                if (location.facing().equals(direction))
                    return location;
            }
        }

        throw new IllegalArgumentException("Direction must be horizontal.");
    }

    // NeoForge: Wish I had AttachmentHolderHost here, instead of needing a full Level
    public <T extends Level> void enableAll(final T level) {
        locations.forEach((placement) -> {
            final var projector = level.getCapability(FieldProjectorCapabilities.FIELD_PROJECTOR_CONTROL, placement.position().pos());
            if (projector != null && !projector.isActive())
                projector.activate();
        });
    }

    // NeoForge: Wish I had AttachmentHolderHost here, instead of needing a full Level
    // FUTURE: Mojang why do I need a whole Level to use removeBlockEntity. Make an interface pls
    public <T extends Level> void disableAll(final T level) {
        locations.forEach((placement) -> {
            final var projector = level.getCapability(FieldProjectorCapabilities.FIELD_PROJECTOR_CONTROL, placement.position().pos());
            if (projector != null && projector.isActive())
                projector.deactivate();
        });
    }
}
