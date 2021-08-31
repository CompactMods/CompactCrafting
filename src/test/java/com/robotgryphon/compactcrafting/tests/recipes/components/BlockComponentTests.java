package com.robotgryphon.compactcrafting.tests.recipes.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.compactcrafting.api.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.components.BlockComponent;
import com.robotgryphon.compactcrafting.recipes.components.ComponentRegistration;
import com.robotgryphon.compactcrafting.tests.util.FileHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class BlockComponentTests {

    @Test
    @Tag("minecraft")
    void CanMatchBlock() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent matcher = res.getFirst();

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
                        if (bs.getValue(StairsBlock.HALF) == Half.TOP)
                            Assertions.fail("Found a state with an invalid property TOP");

                        if (bs.getValue(StairsBlock.SHAPE) != StairsShape.STRAIGHT)
                            Assertions.fail("Found a state with a non-straight shape");
                    }
                });
    }

    @Test
    @Tag("minecraft")
    void CanMatchBlockNoProperties() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent matcher = res.getFirst();

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

                    Assertions.assertEquals(tests.length, matched.size(), "Matches does not equal number of states.");
                });
    }

    @Test
    @Tag("minecraft")
    void CanReserializeComponentMatcher() throws RuntimeException {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent matcher = res.getFirst();

                    BlockComponent.CODEC
                            .encodeStart(JsonOps.INSTANCE, matcher)
                            .resultOrPartial(Assertions::fail)
                            .get();
                });
    }

    @Test
    @Tag("minecraft")
    void ThrowsErrorOnUnregisteredBlock() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_not_registered.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .result()
                .ifPresent(res -> {
                    Assertions.fail("Successfully built a component for an unregistered block.");
                });
    }

    @Test
    @Tag("minecraft")
    void DoesWarnOnBadProperty() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_bad_property.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    Assertions.assertFalse(comp.hasFilter("nonexistent"));
                });
    }

    @Test
    @Tag("minecraft")
    void DoesNotMatchDifferentBlocks() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    boolean matchesAnvil = comp.matches(Blocks.ANVIL.defaultBlockState());
                    Assertions.assertFalse(matchesAnvil, "Expected stairs to not match an anvil.");
                });
    }

    @Test
    @Tag("minecraft")
    void HasCorrectComponentType() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();

                    Assertions.assertNotNull(type);
                    Assertions.assertEquals(ComponentRegistration.BLOCK_COMPONENT.get(), type);
                });
    }

    @Test
    @Tag("minecraft")
    void HasARenderBlockstate() {
        // Loads a cobblestone stairs definition
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/block/block_no_properties.json");

        BlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    BlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    Assertions.assertNotNull(renderState);
                });
    }
}
