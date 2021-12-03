package dev.compactmods.crafting.tests.recipes.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class EmptyComponentTests {

    @Test
    @Tag("minecraft")
    void CanCreate() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/empty/empty_component.json");

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
    @Tag("minecraft")
    void DoesNotErrorRendering() {
        EmptyBlockComponent c = new EmptyBlockComponent();
        boolean errored = c.didErrorRendering();

        Assertions.assertFalse(errored);
    }

    @Test
    @Tag("minecraft")
    void HasComponentType() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    RecipeComponentType<?> type = comp.getType();

                    Assertions.assertNotNull(type);
                    Assertions.assertEquals(ComponentRegistration.EMPTY_BLOCK_COMPONENT.get(), type);
                });
    }

    @Test
    @Tag("minecraft")
    void HasRenderState() {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile("components/empty/empty_component.json");

        EmptyBlockComponent.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(Assertions::fail)
                .ifPresent(res -> {
                    EmptyBlockComponent comp = res.getFirst();

                    BlockState renderState = comp.getRenderState();

                    Assertions.assertNotNull(renderState);
                });
    }

    @Test
    @Tag("minecraft")
    void CanGetBlock() {
        EmptyBlockComponent component = new EmptyBlockComponent();
        Assertions.assertNotNull(component);

        final Block block = Assertions.assertDoesNotThrow(component::getBlock);
        Assertions.assertTrue(block instanceof AirBlock);
    }
}
