package dev.compactmods.crafting.tests.recipes.components;

import java.util.*;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class BlockComponentTests {

    @GameTest(template = "empty_medium")
    public static void CanCreateInstanceWithBlock(final GameTestHelper test) {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);
        if (!Blocks.GOLD_BLOCK.equals(component.getBlock()))
            test.fail("Expected component block to be gold");

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void CanFetchFirstMatch(final GameTestHelper test) {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        try {
            final Optional<BlockState> blockState = component.getFirstMatch();
            if (blockState.isEmpty())
                test.fail("Expected a state to be present.");

            if (!Blocks.GOLD_BLOCK.defaultBlockState().equals(blockState.get()))
                test.fail("Expected first match to be a gold block.");
        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void ToStringShowsBlockId(final GameTestHelper test) {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        try {
            String toString = component.toString();
            if (!toString.contains("minecraft:gold_block"))
                test.fail("Expected block identifier in component toString output");
        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void CanMatchBlock(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_properties.json");

        final var parseResult = BlockComponent.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail);

        if (parseResult.isEmpty()) {
            test.fail("Expected a result; got nothing");
            return;
        }

        final var matcher = parseResult.get();

        BlockState[] tests = Blocks.COBBLESTONE_STAIRS
                .getStateDefinition()
                .getPossibleStates()
                .toArray(new BlockState[0]);

        Hashtable<BlockState, Boolean> results = new Hashtable<>();
        for (BlockState stateTest : tests) {
            boolean matched = matcher.matches(stateTest);
            results.put(stateTest, matched);
        }

        List<BlockState> matched = new ArrayList<>();
        for (Map.Entry<BlockState, Boolean> e : results.entrySet()) {
            if (e.getValue())
                matched.add(e.getKey());
        }

        for (BlockState bs : matched) {
            if (bs.getValue(StairBlock.HALF) == Half.TOP)
                test.fail("Found a state with an invalid property TOP");

            if (bs.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT)
                test.fail("Found a state with a non-straight shape");
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void CanMatchBlockNoProperties(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(matcher -> {
                    BlockState[] tests = Blocks.COBBLESTONE_STAIRS
                            .getStateDefinition()
                            .getPossibleStates()
                            .toArray(new BlockState[0]);

                    Hashtable<BlockState, Boolean> results = new Hashtable<>();
                    for (BlockState stateTest : tests) {
                        boolean matched = matcher.matches(stateTest);
                        results.put(stateTest, matched);
                    }

                    List<BlockState> matched = new ArrayList<>();
                    for (Map.Entry<BlockState, Boolean> e : results.entrySet()) {
                        if (e.getValue())
                            matched.add(e.getKey());
                    }

                    if (tests.length != matched.size())
                        test.fail("Matches does not equal number of states.");

                    test.succeed();
                });
    }

    @GameTest(template = "empty_medium")
    public static void CanReserializeComponentMatcher(final GameTestHelper test) throws RuntimeException {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_properties.json");

        BlockComponent.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(matcher -> {
                    var sout = BlockComponent.CODEC
                            .encodeStart(JsonOps.INSTANCE, matcher)
                            .resultOrPartial(test::fail)
                            .get();

                    if (!sout.equals(json))
                        test.fail("Output JSON did not match input");

                    test.succeed();
                });
    }

    @GameTest(template = "empty_medium")
    public static void ThrowsErrorOnUnregisteredBlock(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_not_registered.json");

        var r = BlockComponent.CODEC
                .decode(JsonOps.INSTANCE, json)
                .result();

        if(r.isPresent()) {
            test.fail("Successfully built a component for an unregistered block.");
        } else {
            test.succeed();
        }
    }

    @GameTest(template = "empty_medium")
    public static void DoesWarnOnBadProperty(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_bad_property.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    if (comp.hasFilter("nonexistent")) {
                        test.fail("Block component was built with an impossible property filter.");
                    }

                    test.succeed();
                });
    }

    @GameTest(template = "empty_medium")
    public static void DoesNotMatchDifferentBlocks(final GameTestHelper test) {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    boolean matchesAnvil = comp.matches(Blocks.ANVIL.defaultBlockState());
                    Assertions.assertFalse(matchesAnvil, "Expected stairs to not match an anvil.");
                });
    }

    @GameTest(template = "empty_medium")
    public static void HasCorrectComponentType(final GameTestHelper test) {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();
                    if(type == null)
                        test.fail("Got a null response from component getType call");

                    if(ComponentRegistration.BLOCK_COMPONENT.get() != type)
                        test.fail("Expected block component type.");

                    test.succeed();
                });
    }

    @GameTest(template = "empty_medium")
    public static void HasARenderBlockstate(final GameTestHelper test) {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    if(renderState == null)
                        test.fail("Expected a blockstate from the renderstate method, got null");

                    test.succeed();
                });
    }

    @GameTest(template = "empty_medium")
    public static void CanHandleErrorRenderingChanges(final GameTestHelper test) {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        boolean error1 = component.didErrorRendering();
        if(error1) {
            test.fail("Expected the renderer to not error on first pass");
        }

        component.markRenderingErrored();
        if(!component.didErrorRendering())
            test.fail("Component did not change after marked error rendering.");

        test.succeed();
    }
}
