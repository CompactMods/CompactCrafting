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
    LARGE(3, 7, "large");

    private int size;

    /**
     * Number of blocks between two projectors.
     */
    private int offset;

    private String name;

    FieldProjectionSize(int size, int offset, String name) {
        this.size = size;
        this.offset = offset;
        this.name = name;
    }

    public int getOffset() {
        return this.offset;
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
