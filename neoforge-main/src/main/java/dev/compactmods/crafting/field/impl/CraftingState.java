package dev.compactmods.crafting.field.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.data.CodecHolder;

import java.util.Optional;

public record CraftingState(Optional<MatchedMiniaturizationRecipe> matchedRecipe, int craftingProgress)
        implements CodecHolder<CraftingState> {

    public static final Codec<CraftingState> CODEC = RecordCodecBuilder.create(i -> i.group(
            MatchedMiniaturizationRecipe.CODEC.optionalFieldOf("recipe").forGetter(CraftingState::matchedRecipe),
            Codec.INT.fieldOf("progress").forGetter(CraftingState::craftingProgress)
    ).apply(i, CraftingState::new));

    public static CraftingState create() {
        return new CraftingState(Optional.empty(), 0);
    }

    @Override
    public Codec<CraftingState> codec() {
        return CODEC;
    }
}
