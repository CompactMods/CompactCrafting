package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Provides utilities to help with projector projectors management.
 */
public abstract class FieldHelper {
    public static void spawnParticlesAtProjectors(IMiniaturizationField field, LevelAccessor level, ParticleOptions opts) {
        final var projectors = field.getProjectors();
        projectors.locations().forEach(proj -> {
            var center = Vec3.atCenterOf(proj.position().pos());
            for (int i = 0; i < 10; i++) {
                RandomSource random = level.getRandom();
                level.addParticle(opts,
                        center.x + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        center.y + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        center.z + ((random.nextBoolean() ? 1 : -1) * random.nextDouble()),
                        0.0, 0.0, 0.0);
            }
        });
    }
}