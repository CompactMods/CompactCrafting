package dev.compactmods.crafting.tests.recipes.components;

import java.util.Map;
import java.util.Optional;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CCRecipeComponentsTests {

    @Test
    void CanCreateInstance() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        Assertions.assertNotNull(components);
    }

    @Test
    void CanFetchComponentsFresh() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final Map<String, IRecipeBlockComponent> blockComponents = components.getBlockComponents();
        final Map<String, IRecipeComponent> allComponents = components.getAllComponents();

        Assertions.assertNotNull(blockComponents);
        Assertions.assertNotNull(allComponents);

        // Both maps must be empty with a new instance
        Assertions.assertEquals(0, blockComponents.size());
        Assertions.assertEquals(0, allComponents.size());
        Assertions.assertTrue(blockComponents.isEmpty());
        Assertions.assertTrue(allComponents.isEmpty());
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanRegisterAndFetchBlocks(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final BlockComponent BLOCK_COMPONENT = new BlockComponent(Blocks.GOLD_BLOCK);
        components.registerBlock("G", BLOCK_COMPONENT);

        if(!components.hasBlock("G"))
            test.fail("Block not registered.");

        final Optional<IRecipeBlockComponent> block = components.getBlock("G");
        if(block.isEmpty())
            test.fail("Did not find an expected component registered for 'G'");

        block.ifPresent(comp -> {
            // If the component is found, make sure it matches our original
            if(!BLOCK_COMPONENT.equals(comp))
                test.fail("Expected component to match");

            // Also check the default block state, since no filters are applied
            if(!comp.matches(Blocks.GOLD_BLOCK.defaultBlockState()))
                test.fail("Expected component to match a specific state; did not match");

            test.succeed();
        });
    }

    @Test
    void EmptyBlocksAreActuallyEmpty() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        components.registerBlock("E", new EmptyBlockComponent());

        Assertions.assertFalse(components.isEmptyBlock("G"), "gold block should not be empty");
        Assertions.assertTrue(components.isEmptyBlock("E"), "empty block not actually empty");
    }

    @Test
    void UnregisteredBlocksAreConsideredEmpty() {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        Assertions.assertTrue(components.isEmptyBlock("A"), "Unregistered block not considered empty");

        final Optional<IRecipeBlockComponent> u = components.getBlock("U");
        u.ifPresent(b -> Assertions.fail("Unregistered block is present."));

        Assertions.assertEquals(Optional.empty(), u);
    }

    @Test
    void CanRegisterNonBlockComponents() {
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

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanGetNumberOfComponents(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        if(0 != components.size())
            test.fail("Newly created component set should be empty");

        // Make sure changes affect the count
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        if(1 != components.size())
            test.fail("Expected a single component registered.");

        // Also make sure "other" components affect the count
        components.registerOther("O", new IRecipeComponent() {
            @Override
            public RecipeComponentType<?> getType() {
                return null;
            }
        });

        if(2 != components.size())
            test.fail("Expected 2 components registered after other type registration.");

        test.succeed();
    }
}
