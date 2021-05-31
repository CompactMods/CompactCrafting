package com.robotgryphon.compactcrafting.field;

public enum FieldProjectionSize {
    /**
     * 3x3x3 Crafting Field Size (Magnitude 1)
     */
    SMALL(1, 3, "small"),

    /**
     * 5x5x5 Crafting Field Size (Magnitude 2)
     */
    MEDIUM(2, 5, "medium"),

    /**
     * 7x7x7 Crafting Field Size (Magnitude 3)
     */
    LARGE(3, 7, "large"),

    ABSURD(4, 9, "absurd");

    private int size;

    /**
     * Number of blocks between two projectors.
     */
    private int projectorDistance;

    private String name;

    FieldProjectionSize(int size, int distance, String name) {
        this.size = size;
        this.projectorDistance = distance;
        this.name = name;
    }

    /**
     * Gets the distance between the center of a field and a projector. (exclusive)
     *
     * @return
     */
    public int getProjectorDistance() {
        return this.projectorDistance;
    }

    public int getDimensions() {
        return (this.size * 2) + 1;
    }

    public int getSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }

    public static FieldProjectionSize maximum() {
        return LARGE;
    }

}
