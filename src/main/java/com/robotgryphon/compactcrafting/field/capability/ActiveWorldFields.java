package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
        Set<IMiniaturizationField> loaded = fields.values().stream()
                .filter(IMiniaturizationField::isLoaded)
                .collect(Collectors.toSet());

        CompactCrafting.LOGGER.trace("Loaded count ({}): {}", level.dimension().location(), loaded.size());
        loaded.forEach(f -> f.tick(level));
    }

    public void registerField(IMiniaturizationField field) {
        BlockPos center = field.getCenter();
        fields.put(center, field);

        LazyOptional<IMiniaturizationField> lazy = LazyOptional.of(() -> field);
        laziness.put(center, lazy);

        lazy.addListener(lo -> {
            lo.ifPresent(this::unregisterField);
        });
    }

    public void unregisterField(IMiniaturizationField field) {
        BlockPos center = field.getCenter();
        fields.remove(center);
        laziness.remove(center);
    }

    public LazyOptional<IMiniaturizationField> getLazy(BlockPos center) {
        return laziness.getOrDefault(center, LazyOptional.empty());
    }

    @Override
    public Optional<IMiniaturizationField> get(BlockPos center) {
        return Optional.ofNullable(fields.getOrDefault(center, null));
    }

    @Override
    public boolean hasActiveField(BlockPos center) {
        return fields.containsKey(center);
    }
}
