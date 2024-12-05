package dev.compactmods.crafting.test.junit.components;

import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Optional;

@ExtendWith(EphemeralTestServerProvider.class)
public class CCRecipeComponentsTests {

    @Test
    public void fetches_fresh_components() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final Map<String, IRecipeBlockComponent> blockComponents = components.getBlockComponents();
        final Map<String, IRecipeComponent> allComponents = components.getAllComponents();

        if (blockComponents == null || allComponents == null)
            Assertions.fail("Components were not empty on first creation.");

        // Both maps must be empty with a new instance
        Assertions.assertEquals(0, blockComponents.size());
        Assertions.assertEquals(0, allComponents.size());
        Assertions.assertTrue(blockComponents.isEmpty());
        Assertions.assertTrue(allComponents.isEmpty());
    }

    @Test
    public void registers_and_fetches_blocks() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final BlockComponent BLOCK_COMPONENT = new BlockComponent(Blocks.GOLD_BLOCK);
        components.registerBlock("G", BLOCK_COMPONENT);

        if (!components.hasBlock("G"))
            Assertions.fail("Block not registered.");

        final Optional<IRecipeBlockComponent> block = components.getBlock("G");
        if (block.isEmpty())
            Assertions.fail("Did not find an expected component registered for 'G'");

        block.ifPresent(comp -> {
            // If the component is found, make sure it matches our original
            if (!BLOCK_COMPONENT.equals(comp))
                Assertions.fail("Expected component to match");

            // Also check the default block state, since no filters are applied
            if (!comp.matches(Blocks.GOLD_BLOCK.defaultBlockState()))
                Assertions.fail("Expected component to match a specific state; did not match");
        });
    }

    @Test
    public void empty_blocks_actually_empty() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        components.registerBlock("E", new EmptyBlockComponent());

        if (components.isEmptyBlock("G"))
            Assertions.fail("gold block should not be empty");

        if (!components.isEmptyBlock("E"))
            Assertions.fail("empty block not actually empty");
    }

    @Test
    public void unregistered_blocks_considered_empty() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        Assertions.assertTrue(components.isEmptyBlock("A"), "Unregistered block not considered empty");

        final Optional<IRecipeBlockComponent> u = components.getBlock("U");
        u.ifPresent(b -> Assertions.fail("Unregistered block is present."));

        Assertions.assertEquals(Optional.empty(), u);
    }

    @Test
    public void registers_non_block_components() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        Assertions.assertDoesNotThrow(() -> {
            components.registerOther("A", new IRecipeComponent() {
                @Override
                public RecipeComponentType<?> getType() {
                    return null;
                }
            });
        }, "Failed to register a component as 'other'.");
    }

    @Test
    public void CanGetNumberOfComponents() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        if (0 != components.size())
            Assertions.fail("Newly created component set should be empty");

        // Make sure changes affect the count
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        if (1 != components.size())
            Assertions.fail("Expected a single component registered.");

        // Also make sure "other" components affect the count
        components.registerOther("O", new IRecipeComponent() {
            @Override
            public RecipeComponentType<?> getType() {
                return null;
            }
        });

        if (2 != components.size())
            Assertions.fail("Expected 2 components registered after other type registration.");
    }
}
