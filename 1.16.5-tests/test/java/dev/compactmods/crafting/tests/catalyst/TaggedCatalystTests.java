package dev.compactmods.crafting.tests.catalyst;

import java.util.Set;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TaggedCatalystTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    @Tag("minecraft")
    void CanCreate() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);
        Assertions.assertNotNull(matcher);

        Assertions.assertDoesNotThrow(matcher::getCodec);
        Assertions.assertDoesNotThrow(matcher::getType);
    }

    @Test
    @Tag("minecraft")
    void NullTagGivesEmptyPossible() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(null);
        final Set<ItemStack> possible = Assertions.assertDoesNotThrow(matcher::getPossible);
        Assertions.assertTrue(possible.isEmpty());
    }

    @Test
    @Tag("minecraft")
    void CanMatchPlanks() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        Assertions.assertTrue(matcher.matches(new ItemStack(Items.OAK_PLANKS)));
        Assertions.assertTrue(matcher.matches(new ItemStack(Items.SPRUCE_PLANKS)));
        Assertions.assertTrue(matcher.matches(new ItemStack(Items.ACACIA_PLANKS)));
    }

    @Test
    @Tag("minecraft")
    void CanFetchPossible() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        final Set<ItemStack> possible = Assertions.assertDoesNotThrow(matcher::getPossible);

        Assertions.assertEquals(ItemTags.PLANKS.getValues().size(), possible.size());
    }
}
