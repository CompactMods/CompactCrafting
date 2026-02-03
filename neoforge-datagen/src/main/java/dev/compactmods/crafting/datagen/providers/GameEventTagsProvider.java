package dev.compactmods.crafting.datagen.providers;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.recipe.CCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class GameEventTagsProvider extends net.minecraft.data.tags.GameEventTagsProvider {
    public GameEventTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CompactCrafting.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        final var builder = this.tag(CCTags.FIELD_CHANGE_EVENTS);
        builder.add(GameEvent.BLOCK_CHANGE.key());
        builder.add(GameEvent.BLOCK_PLACE.key());
        builder.add(GameEvent.BLOCK_DESTROY.key());
        builder.add(GameEvent.FLUID_PLACE.key());
        builder.add(GameEvent.FLUID_PICKUP.key());
    }
}
