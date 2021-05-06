package com.robotgryphon.compactcrafting.test.minecraft.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import com.robotgryphon.compactcrafting.tests.util.FileHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ComponentMatcherTests {

    @Test
    void CanMatchBlock() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("block_properties.json");

        RecipeBlockStateComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    RecipeBlockStateComponent matcher = res.getFirst();

                    BlockState[] tests = Blocks.COBBLESTONE_STAIRS
                            .getStateDefinition()
                            .getPossibleStates()
                            .toArray(new BlockState[0]);

                    Hashtable<BlockState, Boolean> results = new Hashtable<>();
                    for(BlockState stateTest : tests) {
                        boolean matched = matcher.filterMatches(stateTest);
                        results.put(stateTest, matched);
                    }

                    List<BlockState> matched = new ArrayList<>();
                    for(Map.Entry<BlockState, Boolean> e : results.entrySet()) {
                        if(e.getValue())
                            matched.add(e.getKey());
                    }

                    for(BlockState bs : matched) {
                        if(bs.getValue(StairsBlock.HALF) == Half.TOP)
                            Assertions.fail("Found a state with an invalid property TOP");

                        if(bs.getValue(StairsBlock.SHAPE) != StairsShape.STRAIGHT)
                            Assertions.fail("Found a state with a non-straight shape");
                    }
                });
    }

    @Test
    void CanMatchBlockNoProperties() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("block_no_properties.json");

        RecipeBlockStateComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    RecipeBlockStateComponent matcher = res.getFirst();

                    BlockState[] tests = Blocks.COBBLESTONE_STAIRS
                            .getStateDefinition()
                            .getPossibleStates()
                            .toArray(new BlockState[0]);

                    Hashtable<BlockState, Boolean> results = new Hashtable<>();
                    for(BlockState stateTest : tests) {
                        boolean matched = matcher.filterMatches(stateTest);
                        results.put(stateTest, matched);
                    }

                    List<BlockState> matched = new ArrayList<>();
                    for(Map.Entry<BlockState, Boolean> e : results.entrySet()) {
                        if(e.getValue())
                            matched.add(e.getKey());
                    }

                    Assertions.assertEquals(tests.length, matched.size(), "Matches does not equal number of states.");
                });
    }

    @Test
    void CanReserializeComponentMatcher() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("block_properties.json");

        RecipeBlockStateComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    RecipeBlockStateComponent matcher = res.getFirst();

                    RecipeBlockStateComponent.CODEC
                            .encodeStart(JsonOps.INSTANCE, matcher)
                            .resultOrPartial(Assertions::fail)
                            .ifPresent(jsonE -> {
                                CompactCrafting.LOGGER.info(jsonE);
                            });
                });
    }
}
