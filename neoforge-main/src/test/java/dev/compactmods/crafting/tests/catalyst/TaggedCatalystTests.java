package dev.compactmods.crafting.tests.catalyst;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.catalyst.ItemTagCatalystMatcher;
import dev.compactmods.crafting.tests.GameTestTemplates;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class TaggedCatalystTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreate(final GameTestHelper test) {
        new ItemTagCatalystMatcher(ItemTags.PLANKS);
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void NullTagGivesEmptyPossible(final GameTestHelper test) {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(null);
        final Set<ItemStack> possible = matcher.getPossible();
        if(!possible.isEmpty())
            test.fail("Null tag should give no results.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void matches_planks(final GameTestHelper test) {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        if(!matcher.matches(new ItemStack(Items.OAK_PLANKS))) test.fail("Matcher failed on a plank type.");
        if(!matcher.matches(new ItemStack(Items.SPRUCE_PLANKS))) test.fail("Matcher failed on a plank type.");
        if(!matcher.matches(new ItemStack(Items.ACACIA_PLANKS))) test.fail("Matcher failed on a plank type.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanFetchPossible(final GameTestHelper test) {
        ItemTagCatalystMatcher matcher = new ItemTagCatalystMatcher(ItemTags.PLANKS);

        final Set<ItemStack> possible = matcher.getPossible();

        final var it = ForgeRegistries.ITEMS.tags();
        if(it.getTag(ItemTags.PLANKS).size() != possible.size())
            test.fail("Planks size versus possible size did not match.");

        test.succeed();
    }
}
