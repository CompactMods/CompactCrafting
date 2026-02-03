package dev.compactmods.crafting.field.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.util.CCExtraCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

// Crafting State
public record MatchedMiniaturizationRecipe(Identifier recipe, StructureTemplate matchedBlocks) {
    public static final Codec<MatchedMiniaturizationRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("recipe_id").forGetter(MatchedMiniaturizationRecipe::recipe),
            CCExtraCodecs.STRUCTURE_TEMPLATE_CODEC.fieldOf("matched_blocks").forGetter(MatchedMiniaturizationRecipe::matchedBlocks)
    ).apply(i, MatchedMiniaturizationRecipe::new));
}
