package com.robotgryphon.compactcrafting.field.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class ActiveWorldFields implements IActiveWorldFields {

    /**
     * Holds a set of miniaturization fields that are active, referenced by their center point.
     */
    private HashMap<BlockPos, IMiniaturizationField> fields;
    private HashMap<BlockPos, LazyOptional<IMiniaturizationField>> laziness;

    public ActiveWorldFields() {
        this.fields = new HashMap<>();
        this.laziness = new HashMap<>();
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

        LazyOptional<IMiniaturizationField> lazy = LazyOptional.of(() -> field);
        laziness.put(center, lazy);

        lazy.addListener(lo -> {
            lo.ifPresent(this::deactivateField);
        });
    }

    public void deactivateField(IMiniaturizationField field) {
        BlockPos center = field.getCenterPosition();
        fields.remove(center);
    }

    public LazyOptional<IMiniaturizationField> getLazy(BlockPos center) {
        return laziness.getOrDefault(center, LazyOptional.empty());
    }

    @Override
    public Optional<IMiniaturizationField> get(BlockPos center) {
        return Optional.ofNullable(fields.getOrDefault(center, null));
    }
}
