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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class EmptyComponentTests {

    @GameTest(template = "empty")
    public static void CanCreateEmptyComponent(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    boolean matchesAir = comp.matches(Blocks.AIR.defaultBlockState());
                    boolean matchesCaveAir = comp.matches(Blocks.CAVE_AIR.defaultBlockState());

                    if (!matchesAir)
                        test.fail("Expected empty to match air.");

                    if (!matchesCaveAir)
                        test.fail("Expected empty to match cave air.");

                    test.succeed();
                });
    }

    @Test
    void DoesNotErrorRendering() {
        EmptyBlockComponent c = new EmptyBlockComponent();
        boolean errored = c.didErrorRendering();

        Assertions.assertFalse(errored);
    }

    @GameTest(template = "empty")
    public static void HasComponentType(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();

                    if (type == null)
                        test.fail("Got null component type");

                    if (!ComponentRegistration.EMPTY_BLOCK_COMPONENT.get().equals(type))
                        test.fail("Expected correct type");

                    test.succeed();
                });
    }

    @GameTest(template = "empty")
    public static void HasRenderState(final GameTestHelper test) {
        JsonElement json = FileHelper.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(test::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    if (renderState == null)
                        test.fail("Got null render state back from component");

                    test.succeed();
                });
    }

    @GameTest(template = "empty")
    public static void CanGetBlock(final GameTestHelper test) {
        EmptyBlockComponent component = new EmptyBlockComponent();
        final Block block = component.getBlock();

        if (block instanceof AirBlock) {
            test.succeed();
            return;
        }

        test.fail("Empty block is not air.");
    }
}
