package dev.compactmods.crafting.test.junit.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.test.FileHelper;
import dev.compactmods.crafting.test.junit.recipes.util.JUnitTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class BlockComponentTests {

    @Test
    public void CanCreateInstanceWithBlock() {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);
        if (!Blocks.GOLD_BLOCK.equals(component.getBlock()))
            Assertions.fail("Expected component block to be gold");
    }

    @Test
    public void CanFetchFirstMatch() {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        try {
            final Optional<BlockState> blockState = component.getFirstMatch();
            if (blockState.isEmpty())
                Assertions.fail("Expected a state to be present.");

            if (!Blocks.GOLD_BLOCK.defaultBlockState().equals(blockState.get()))
                Assertions.fail("Expected first match to be a gold block.");
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void ToStringShowsBlockId() {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        try {
            String toString = component.toString();
            if (!toString.contains("minecraft:gold_block"))
                Assertions.fail("Expected block identifier in component toString output");
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

    @Test
    public void CanMatchBlock() {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_properties.json");

        final var parseResult = BlockComponent.CODEC.codec()
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail);

        if (parseResult.isEmpty()) {
            Assertions.fail("Expected a result; got nothing");
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
                Assertions.fail("Found a state with an invalid property TOP");

            if (bs.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT)
                Assertions.fail("Found a state with a non-straight shape");
        }

    }

    @Test
    public void CanMatchBlockNoProperties() {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.codec()
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
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
                        Assertions.fail("Matches does not equal number of states.");

                });
    }

    @Test
    public void CanReserializeComponentMatcher() {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_properties.json");

        BlockComponent.CODEC.codec()
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(matcher -> {
                    var sout = BlockComponent.CODEC
                            .codec()
                            .encodeStart(JsonOps.INSTANCE, matcher)
                            .resultOrPartial(Assertions::fail)
                            .get();

                    if (!sout.equals(json))
                        Assertions.fail("Output JSON did not match input");

                });
    }

    @Test
    public void ThrowsErrorOnUnregisteredBlock() {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_not_registered.json");

        var r = BlockComponent.CODEC
                .codec()
                .decode(JsonOps.INSTANCE, json)
                .result();

        if (r.isPresent()) {
            Assertions.fail("Successfully built a component for an unregistered block.");
        } else {
        }
    }

    @Test
    public void DoesWarnOnBadProperty() {
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_bad_property.json");

        BlockComponent.CODEC.codec()
                .decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    if (comp.hasFilter("nonexistent")) {
                        Assertions.fail("Block component was built with an impossible property filter.");
                    }

                });
    }

    @Test
    public void DoesNotMatchDifferentBlocks() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.codec()
                .decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    boolean matchesAnvil = comp.matches(Blocks.ANVIL.defaultBlockState());
                    if (matchesAnvil)
                        Assertions.fail("Expected stairs to not match an anvil.");
                });

    }

    @Test
    public void HasCorrectComponentType() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.codec()
                .decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();
                    if (type == null)
                        Assertions.fail("Got a null response from component getType call");

                    if (ComponentRegistration.BLOCK_COMPONENT.get() != type)
                        Assertions.fail("Expected block component type.");

                });
    }

    @Test
    public void HasARenderBlockstate() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.codec()
                .decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    if (renderState == null)
                        Assertions.fail("Expected a blockstate from the renderstate method, got null");

                });
    }

    @Test
    public void CanHandleErrorRenderingChanges() {
        BlockComponent component = new BlockComponent(Blocks.GOLD_BLOCK);

        boolean error1 = component.didErrorRendering();
        if (error1) {
            Assertions.fail("Expected the renderer to not error on first pass");
        }

        component.markRenderingErrored();
        if (!component.didErrorRendering())
            Assertions.fail("Component did not change after marked error rendering.");

    }
}
