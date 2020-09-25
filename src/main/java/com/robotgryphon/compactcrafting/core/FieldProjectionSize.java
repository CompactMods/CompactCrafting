package com.robotgryphon.compactcrafting.core;

public enum FieldProjectionSize {
    /**
     * 3x3x3 Crafting Field Size (Magnitude 1)
     */
    SMALL(1, "small"),

    /**
     * 5x5x5 Crafting Field Size (Magnitude 2)
     */
    MEDIUM(2, "medium"),

    /**
     * 7x7x7 Crafting Field Size (Magnitude 3)
     */
    LARGE(3, "large");

    private int size;
    private String name;
    FieldProjectionSize(int size, String name) {
        this.size = size;
        this.name = name;
    }

    public int getSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }
}
