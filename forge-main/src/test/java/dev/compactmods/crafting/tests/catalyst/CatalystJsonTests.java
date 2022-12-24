package dev.compactmods.crafting.tests.catalyst;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.recipes.catalyst.CatalystMatcherCodec;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class CatalystJsonTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void ChoosesCorrectCatalystCodec(final GameTestHelper test) {
        JsonElement node = new JsonPrimitive("compactcrafting:item");

        final Optional<CatalystType<?>> catalyst = CatalystMatcherCodec.INSTANCE.parse(JsonOps.INSTANCE, node)
                .resultOrPartial(test::fail);

        if(catalyst.isEmpty())
            test.fail("Did not choose catalyst matcher correctly.");

        final CatalystType<?> type = catalyst.get();
        final var correctType = type instanceof ItemStackCatalystMatcher;
        if(!correctType)
            test.fail("Expected the matcher to create an IS matcher instance; got " + type.getClass().getName());

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void FailsDecodeOnUnmatchedCatalystType(final GameTestHelper test) {
        JsonElement node = new JsonPrimitive("compactcrafting:nonexistent");

        final Optional<DataResult.PartialResult<CatalystType<?>>> catalyst = CatalystMatcherCodec.INSTANCE
                .parse(JsonOps.INSTANCE, node)
                .error();

        if(catalyst.isEmpty())
            test.fail("Matched catalyst that was nonexistent");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void LoadsItemCatalystFromJson(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("catalysts/item_no_nbt.json");

        final Optional<ICatalystMatcher> catalyst = CatalystMatcherCodec.MATCHER_CODEC
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail);

        if(catalyst.isEmpty()) {
            test.fail("Did not deserialize catalyst matcher correctly.");
            return;
        }

        final ICatalystMatcher matcher = catalyst.get();
        if(!(matcher instanceof ItemStackCatalystMatcher))
            test.fail("Matcher did not choose correct catalyst type");

        boolean matched = matcher.matches(new ItemStack(Items.REDSTONE, 1));
        if(!matched)
            test.fail("ItemStack matcher did not successfully match correct item.");

        test.succeed();
    }

}
