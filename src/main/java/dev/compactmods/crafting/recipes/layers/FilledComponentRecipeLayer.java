package dev.compactmods.crafting.recipes.layers;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class FilledComponentRecipeLayer implements IRecipeLayer, IDynamicSizedRecipeLayer {

    private String componentKey;
    private AxisAlignedBB recipeDimensions;

    public static final Codec<FilledComponentRecipeLayer> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("component").forGetter(FilledComponentRecipeLayer::getComponent)
        ).apply(in, FilledComponentRecipeLayer::new));

    public FilledComponentRecipeLayer(String component) {
        this.componentKey = component;
    }

    public String getComponent() {
        return this.componentKey;
    }

    @Override
    public Set<String> getComponents() {
        return ImmutableSet.of(componentKey);
    }

    public Map<String, Integer> getComponentTotals() {
        return Collections.singletonMap(componentKey, getNumberFilledPositions());
    }

    public Optional<String> getComponentForPosition(BlockPos pos) {
        if(recipeDimensions.contains(pos.getX(), pos.getY(), pos.getZ()))
            return Optional.ofNullable(componentKey);

        return Optional.empty();
    }

    public int getNumberFilledPositions() {
        if(recipeDimensions == null)
            return 0;

        return (int) Math.ceil(recipeDimensions.getXsize() * recipeDimensions.getZsize());
    }

    @Override
    public boolean matches(IRecipeComponents components, IRecipeLayerBlocks blocks) {
        if(!blocks.allIdentified()) return false;

        Map<String, Integer> totalsInWorld = blocks.getKnownComponentTotals();
        if(totalsInWorld.size() != 1)
            return false;

        if(!totalsInWorld.containsKey(this.componentKey))
            return false;

        return this.getNumberFilledPositions() == totalsInWorld.get(componentKey);
    }

    public RecipeLayerType<?> getType() {
        return Registration.FILLED_LAYER_SERIALIZER.get();
    }

    public void setComponent(String component) {
        this.componentKey = component;
    }

    /**
     * Used to update a recipe layer to change the size of the recipe base.
     *
     * @param dimensions The new dimensions of the recipe.
     */
    public void setRecipeDimensions(AxisAlignedBB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    public void recalculateRequirements() {

    }
}
