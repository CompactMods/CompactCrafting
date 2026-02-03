package dev.compactmods.crafting.field.impl;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.ARGB;

public interface MiniaturizationFieldParticles {
    // Metallic Orange
    DustParticleOptions RECIPE_MATCHED_PARTICLE_OPTS
            = new DustParticleOptions(ARGB.color(210, 226, 99, 16), 1);

    // Eucalyptus (Turquoise Green)
    DustParticleOptions RECIPE_FINISHED_PARTICLE_OPTS
            = new DustParticleOptions(ARGB.color(210, 68, 215, 168), 1);
}
