package dev.compactmods.crafting.api.recipe;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CCTags {

    TagKey<GameEvent> FIELD_CHANGE_EVENTS = TagKey.create(BuiltInRegistries.GAME_EVENT.key(),
            CompactCrafting.identifier("field_change_events"));
}
