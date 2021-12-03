package dev.compactmods.crafting.tests.catalyst;

import java.util.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.recipes.catalyst.CatalystMatcherCodec;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class CatalystJsonTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    @Tag("minecraft")
    void ChoosesCorrectCatalystCodec() {
        JsonElement node = new JsonPrimitive("compactcrafting:item");

        final Optional<CatalystType<?>> catalyst = CatalystMatcherCodec.INSTANCE.parse(JsonOps.INSTANCE, node)
                .resultOrPartial(Assertions::fail);

        if(!catalyst.isPresent())
            Assertions.fail("Did not choose catalyst matcher correctly.");

        final CatalystType<?> type = catalyst.get();
        Assertions.assertTrue(type instanceof ItemStackCatalystMatcher);
    }

    @Test
    @Tag("minecraft")
    void FailsDecodeOnUnmatchedCatalystType() {
        JsonElement node = new JsonPrimitive("compactcrafting:nonexistent");

        final Optional<DataResult.PartialResult<CatalystType<?>>> catalyst = CatalystMatcherCodec.INSTANCE.parse(JsonOps.INSTANCE, node)
                .error();

        Assertions.assertTrue(catalyst.isPresent());
    }

    @Test
    @Tag("minecraft")
    void LoadsItemCatalystFromJson() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("catalysts/item_no_nbt.json");

        final Optional<ICatalystMatcher> catalyst = CatalystMatcherCodec.MATCHER_CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail);

        if(!catalyst.isPresent())
            Assertions.fail("Did not deserialize catalyst matcher correctly.");

        final ICatalystMatcher matcher = catalyst.get();
        Assertions.assertTrue(matcher instanceof ItemStackCatalystMatcher);

        boolean matched = matcher.matches(new ItemStack(Items.REDSTONE, 1));
        Assertions.assertTrue(matched);
    }

}
