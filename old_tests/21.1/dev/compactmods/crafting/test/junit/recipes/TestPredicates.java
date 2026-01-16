package dev.compactmods.crafting.test.junit.recipes;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class TestPredicates {

    @Test
    public void testPredicate(MinecraftServer server) {

        final var predicate = ItemPredicate.Builder.item()
                .of(Items.STICK)
                .withCount(MinMaxBounds.Ints.exactly(1))
                .build();

        final var ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        final var json = ItemPredicate.CODEC.encodeStart(ops, predicate)
                .resultOrPartial()
                .orElseThrow();

        final var stringified = json.toString();
    }
}
