package dev.compactmods.crafting.tests.catalyst;

import java.util.Set;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

// TODO - GameTests
public class ItemStackCatalystTests {

    private ItemStackCatalystMatcher getMatcherFromFile(String s) {
        return ItemStackCatalystMatcher.CODEC
                .parse(JsonOps.INSTANCE, FileHelper.getJsonFromFile(s))
                .getOrThrow(false, Assertions::fail);
    }

    void CanCreate() {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile("catalysts/anvil_renamed_redstone.json");

        final Set<ItemStack> possible = Assertions.assertDoesNotThrow(matcher::getPossible);
        Assertions.assertEquals(1, possible.size());
    }

    void FailsMatchNoNbt() {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile("catalysts/anvil_renamed_redstone.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        // what an anvil does to rename - see AnvilScreen
        testStack.setHoverName(new TextComponent("Renamed Item"));

        final Boolean matched = Assertions.assertDoesNotThrow(() -> matcher.matches(testStack), "Stack match attempt threw exception");

        Assertions.assertTrue(matched);
    }

    void FailsMatchIfStackTagEmpty() {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile("catalysts/item_nbt_nokey.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        final Boolean matched = Assertions.assertDoesNotThrow(() -> matcher.matches(testStack), "Stack match attempt threw exception");

        Assertions.assertFalse(matched, "Stack matched even though it had filters, but stack NBT was empty.");
    }

    void FailsMatchOnUnmatchedFilter() {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile("catalysts/item_nbt_nokey.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        // needed so the stack isn't empty
        testStack.getOrCreateTag().putString("hi", "there");

        final Boolean matched = Assertions.assertDoesNotThrow(() -> matcher.matches(testStack), "Stack match attempt threw exception");

        Assertions.assertFalse(matched, "Stack matched even though a filtered key was not present.");
    }
}
