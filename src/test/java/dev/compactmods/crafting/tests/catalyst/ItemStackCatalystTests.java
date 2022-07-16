package dev.compactmods.crafting.tests.catalyst;

import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ItemStackCatalystTests {

    private static ItemStackCatalystMatcher getMatcherFromFile(GameTestHelper test, String s) {
        return ItemStackCatalystMatcher.CODEC
                .parse(JsonOps.INSTANCE, FileHelper.getJsonFromFile(s))
                .getOrThrow(false, test::fail);
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreate(final GameTestHelper test) {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile(test, "catalysts/anvil_renamed_redstone.json");

        final Set<ItemStack> possible = matcher.getPossible();
        if (1 != possible.size())
            test.fail("Expected one possible state; got " + possible.size());

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void FailsMatchNoNbt(final GameTestHelper test) {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile(test, "catalysts/anvil_renamed_redstone.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        // what an anvil does to rename - see AnvilScreen
        testStack.setHoverName(Component.literal("Renamed Item"));

        final boolean matched = matcher.matches(testStack);
        if (!matched)
            test.fail("Stack match attempt threw exception");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void fails_match_if_stack_tag_empty(final GameTestHelper test) {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile(test, "catalysts/item_nbt_nokey.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        final boolean matched = matcher.matches(testStack);
        if(matched)
            test.fail("Stack matched even though it had filters, but stack NBT was empty.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void fails_match_on_unmatched_filter(final GameTestHelper test) {
        final ItemStackCatalystMatcher matcher = getMatcherFromFile(test, "catalysts/item_nbt_nokey.json");

        ItemStack testStack = new ItemStack(Items.REDSTONE);

        // needed so the stack isn't empty
        testStack.getOrCreateTag().putString("hi", "there");

        final boolean matched = matcher.matches(testStack);
        if(matched)
            test.fail("Stack matched even though a filtered key was not present.");

        test.succeed();
    }
}
