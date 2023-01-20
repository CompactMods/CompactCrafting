package dev.compactmods.crafting.recipes.layers;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IDynamicSizedRecipeLayer;
import dev.compactmods.crafting.core.CCLayerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class EmptyRecipeLayer extends ForgeRegistryEntry<RecipeLayerType<?>>
        implements IRecipeLayer, IDynamicSizedRecipeLayer {

    public static final Codec<EmptyRecipeLayer> CODEC = Codec.unit(EmptyRecipeLayer::new);

    @Override
    public void setRecipeDimensions(AABB dimensions) {

    }

    @Override
    public Set<String> getComponents() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> getComponentForPosition(BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Stream<BlockPos> getPositionsForComponent(String component) {
        return Stream.empty();
    }

    @Override
    public RecipeLayerType<?> getType() {
        return CCLayerTypes.EMPTY_LAYER_TYPE.get();
    }

    @Override
    public boolean matches(IRecipeComponents components, IRecipeBlocks blocks) {
        final boolean allIdentified = blocks.allIdentified();

        // Check 1 - If anything was unidentified, ensure all the states are air
        if(!allIdentified) {
            final boolean anyNonAir = blocks.getUnmappedPositions()
                    .map(blocks::getStateAtPosition)
                    .anyMatch(state -> !state.isAir());

            if(anyNonAir)
                return false;
        }

        // Check 2 - Any of the matched components are non-air
        final boolean anyNonAirKnown = blocks.getKnownComponentTotals().keySet()
                .stream()
                .anyMatch(comp -> !components.isEmptyBlock(comp));

        return !anyNonAirKnown;
    }
}
