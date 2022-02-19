package dev.compactmods.crafting.tests.recipes.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmptyComponentTests {

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanCreate(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    boolean matchesAir = comp.matches(Blocks.AIR.defaultBlockState());
                    boolean matchesCaveAir = comp.matches(Blocks.CAVE_AIR.defaultBlockState());

                    Assertions.assertTrue(matchesAir, "Expected empty to match air.");
                    Assertions.assertTrue(matchesCaveAir, "Expected empty to match cave air.");
                });
    }

    @Test
    void DoesNotErrorRendering() {
        EmptyBlockComponent c = new EmptyBlockComponent();
        boolean errored = c.didErrorRendering();

        Assertions.assertFalse(errored);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void HasComponentType(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();

                    Assertions.assertNotNull(type);
                    Assertions.assertEquals(ComponentRegistration.EMPTY_BLOCK_COMPONENT.get(), type);
                });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void HasRenderState(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    Assertions.assertNotNull(renderState);
                });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanGetBlock(final GameTestHelper test) {
        EmptyBlockComponent component = new EmptyBlockComponent();
        Assertions.assertNotNull(component);

        final Block block = Assertions.assertDoesNotThrow(component::getBlock);
        Assertions.assertTrue(block instanceof AirBlock);
    }
}
