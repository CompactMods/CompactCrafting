package dev.compactmods.crafting.client.render;

import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import dev.compactmods.crafting.client.render.field.FieldRenderInstruction;
import dev.compactmods.crafting.client.render.field.MainFieldOutlineRenderInstruction;
import dev.compactmods.crafting.client.render.field.MainFieldRenderInstruction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class MiniaturizationFieldRenderBuilder {
    private AABB bounds;
    private boolean drawFieldOutlines;
    private boolean drawFieldFaces = true;
    private boolean drawScanLines = true;
    private float outlineWidth;
    private int baseColor;
    private FieldProjectorPlacements projectors;

    private static MiniaturizationFieldRenderBuilder base(int baseColor) {
        final var builder = new MiniaturizationFieldRenderBuilder();
        builder.baseColor = baseColor;
        return builder;
    }

    public static MiniaturizationFieldRenderBuilder forDefault(int baseColor) {
        return base(baseColor)
                .outlined(1)
                .filled();
    }

    public static MiniaturizationFieldRenderBuilder wireframe(int baseColor, float outlineWidth) {
        return base(baseColor)
                .outlined(outlineWidth);
    }

    public MiniaturizationFieldRenderBuilder withFieldBoundaries(AABB bounds) {
        this.bounds = bounds;
        return this;
    }

    public MiniaturizationFieldRenderBuilder withProjectors(FieldProjectorPlacements projectors) {
        this.projectors = projectors;
        return this;
    }

    public MiniaturizationFieldRenderBuilder outlined(float outlineWidth) {
        this.drawFieldOutlines = true;
        this.outlineWidth = outlineWidth;
        return this;
    }
    public MiniaturizationFieldRenderBuilder filled() {
        this.drawFieldFaces = true;
        return this;
    }


    public Set<FieldRenderInstruction> build() {
        final var instr = new ObjectOpenHashSet<FieldRenderInstruction>();

        instr.add(MainFieldRenderInstruction.create(bounds, baseColor));

        if(drawFieldOutlines)
            instr.add(MainFieldOutlineRenderInstruction.create(bounds, outlineWidth, baseColor));

        return instr;
    }
}
