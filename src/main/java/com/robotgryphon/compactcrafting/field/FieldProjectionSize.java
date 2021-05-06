package com.robotgryphon.compactcrafting.field;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FieldProjectionSize {
    /**
     * 3x3x3 Crafting Field Size (Magnitude 1)
     */
    SMALL("small", 1),

    /**
     * 5x5x5 Crafting Field Size (Magnitude 2)
     */
    MEDIUM("medium", 2),

    /**
     * 7x7x7 Crafting Field Size (Magnitude 3)
     */
    LARGE("large", 3),

    ABSURD("absurd", 4);

    private static final ImmutableMap<String, FieldProjectionSize> SIZES = Stream.of(values()).collect(ImmutableMap.toImmutableMap(FieldProjectionSize::getName, Function.identity()));

    private final String name;
    private final int magnitude;
    private final int dimensions;
    private final int projectorOffset;
    private final int oppositeProjectorOffset;

    FieldProjectionSize(String name, int magnitude) {
        this.magnitude = magnitude;
        this.name = name;

        this.dimensions = (this.magnitude * 2) + 1;
        this.projectorOffset = this.dimensions + 1;
        this.oppositeProjectorOffset = (this.magnitude * 4) + 4;
    }

    /**
     * Gets the offset from one projector to the opposing projector.
     * Adding this to a projector in the direction it is facing will return the possible location of the opposing projector at this field size.
     *
     * @return the offset from one projector to the opposing projector.
     */
    public int getOppositeProjectorOffset() {
        return this.oppositeProjectorOffset;
    }

    /**
     * Gets the offset from the center of the field to any one of its projectors.
     * Adding this to the center block position in a specified direction will return the possible location of a projector at this field size.
     *
     * @return the offset from the center of the field to any one of its projectors.
     */
    public int getProjectorOffset() {
        return this.projectorOffset;
    }

    /**
     * Gets the total length of the side of the field.
     *
     * @return the total length of the side of the field.
     */
    public int getDimensions() {
        return this.dimensions;
    }

    /**
     * Gets the magnitude to expand the center block by to get the entire bounding box of the field.
     *
     * @return the magnitude to expand the center block by to get the entire bounding box of the field.
     */
    public int getMagnitude() {
        return this.magnitude;
    }

    public String getName() {
        return this.name;
    }

    public static FieldProjectionSize getMaximumSize() {
        return LARGE;
    }

    public static FieldProjectionSize getSizeByName(String name) {
        return SIZES.get(name);
    }
}
