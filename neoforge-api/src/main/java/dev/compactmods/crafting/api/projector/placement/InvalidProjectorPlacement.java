package dev.compactmods.crafting.api.projector.placement;

public record InvalidProjectorPlacement(ProjectorPlacement placement, Reason reason) {
    public enum Reason {
        Missing,
        Offline,
        NotTargetingField
    }
}
