package dev.compactmods.crafting.tests.catalyst;

import java.util.Set;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

// TODO - GameTests
public class TaggedCatalystTests {

    void CanCreate() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);
        Assertions.assertNotNull(matcher);

        Assertions.assertDoesNotThrow(matcher::getCodec);
        Assertions.assertDoesNotThrow(matcher::getType);
    }

    void NullTagGivesEmptyPossible() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(null);
        final Set<ItemStack> possible = Assertions.assertDoesNotThrow(matcher::getPossible);
        Assertions.assertTrue(possible.isEmpty());
    }

    void CanMatchPlanks() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        Assertions.assertTrue(matcher.matches(new ItemStack(Items.OAK_PLANKS)));
        Assertions.assertTrue(matcher.matches(new ItemStack(Items.SPRUCE_PLANKS)));
        Assertions.assertTrue(matcher.matches(new ItemStack(Items.ACACIA_PLANKS)));
    }

    void CanFetchPossible() {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        final Set<ItemStack> possible = Assertions.assertDoesNotThrow(matcher::getPossible);

        Assertions.assertEquals(ItemTags.PLANKS.getValues().size(), possible.size());
    }
}
