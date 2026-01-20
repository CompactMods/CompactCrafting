package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface FieldProjectorTags {

    TagKey<Block> PROJECTOR_BLOCK = TagKey.create(Registries.BLOCK, CompactCrafting.identifier("field_projector"));

    TagKey<Block> INACTIVE_PROJECTOR_BLOCK = TagKey.create(Registries.BLOCK, CompactCrafting.identifier("inactive_field_projector"));

    TagKey<Block> ACTIVE_PROJECTOR_BLOCK = TagKey.create(Registries.BLOCK, CompactCrafting.identifier("active_field_projector"));
}
