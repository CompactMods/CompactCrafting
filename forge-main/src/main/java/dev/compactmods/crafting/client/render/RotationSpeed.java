package dev.compactmods.crafting.client.render;

public enum RotationSpeed {
    SLOW(5000),
    MEDIUM(2500),
    FAST(1000);

    private int speed;

    RotationSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }
}
