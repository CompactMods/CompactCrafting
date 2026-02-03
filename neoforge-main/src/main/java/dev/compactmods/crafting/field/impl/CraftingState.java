package dev.compactmods.crafting.field.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record CraftingState(Optional<MatchedMiniaturizationRecipe> matchedRecipe,
                     int craftingProgress) {
    public static final Codec<CraftingState> CODEC = RecordCodecBuilder.create(i -> i.group(
            MatchedMiniaturizationRecipe.CODEC.optionalFieldOf("recipe").forGetter(CraftingState::matchedRecipe),
            Codec.INT.fieldOf("progress").forGetter(CraftingState::craftingProgress)
    ).apply(i, CraftingState::new));
}
