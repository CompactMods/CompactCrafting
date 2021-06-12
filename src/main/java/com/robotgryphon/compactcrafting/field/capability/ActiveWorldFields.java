package com.robotgryphon.compactcrafting.field.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.stream.Stream;

public class ActiveWorldFields implements IActiveWorldFields {

    /**
     * Holds a set of miniaturization fields that are active, referenced by their center point.
     */
    private HashMap<BlockPos, IMiniaturizationField> fields;

    public ActiveWorldFields() {
        this.fields = new HashMap<>();
    }

    @Override
    public Stream<IMiniaturizationField> getFields() {
        return fields.values().stream();
    }

    public void tickFields(World level) {
        fields.values().forEach(f -> f.tick(level));
    }

    public void activateField(IMiniaturizationField field) {
        BlockPos center = field.getCenterPosition();
        fields.put(center, field);
    }

    public void deactivateField(IMiniaturizationField field) {
        BlockPos center = field.getCenterPosition();
        fields.remove(center);
    }
}
