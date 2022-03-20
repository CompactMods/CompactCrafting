package dev.compactmods.crafting.tests.recipes.components;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class CCRecipeComponentsTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void fetches_fresh_components(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final Map<String, IRecipeBlockComponent> blockComponents = components.getBlockComponents();
        final Map<String, IRecipeComponent> allComponents = components.getAllComponents();

        if (blockComponents == null || allComponents == null)
            test.fail("Components were not empty on first creation.");

        // Both maps must be empty with a new instance
        GameTestAssertions.assertEquals(0, blockComponents.size());
        GameTestAssertions.assertEquals(0, allComponents.size());
        GameTestAssertions.assertTrue(blockComponents.isEmpty());
        GameTestAssertions.assertTrue(allComponents.isEmpty());

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void registers_and_fetches_blocks(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        final BlockComponent BLOCK_COMPONENT = new BlockComponent(Blocks.GOLD_BLOCK);
        components.registerBlock("G", BLOCK_COMPONENT);

        if (!components.hasBlock("G"))
            test.fail("Block not registered.");

        final Optional<IRecipeBlockComponent> block = components.getBlock("G");
        if (block.isEmpty())
            test.fail("Did not find an expected component registered for 'G'");

        block.ifPresent(comp -> {
            // If the component is found, make sure it matches our original
            if (!BLOCK_COMPONENT.equals(comp))
                test.fail("Expected component to match");

            // Also check the default block state, since no filters are applied
            if (!comp.matches(Blocks.GOLD_BLOCK.defaultBlockState()))
                test.fail("Expected component to match a specific state; did not match");

            test.succeed();
        });
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void empty_blocks_actually_empty(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        components.registerBlock("E", new EmptyBlockComponent());

        if (components.isEmptyBlock("G"))
            test.fail("gold block should not be empty");

        if (!components.isEmptyBlock("E"))
            test.fail("empty block not actually empty");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void unregistered_blocks_considered_empty(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        GameTestAssertions.assertTrue(components.isEmptyBlock("A"), "Unregistered block not considered empty");

        final Optional<IRecipeBlockComponent> u = components.getBlock("U");
        u.ifPresent(b -> test.fail("Unregistered block is present."));

        GameTestAssertions.assertEquals(Optional.empty(), u);

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void registers_non_block_components(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        GameTestAssertions.assertDoesNotThrow(() -> {
            components.registerOther("A", new IRecipeComponent() {
                @Override
                public RecipeComponentType<?> getType() {
                    return null;
                }
            });
        }, "Failed to register a component as 'other'.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanGetNumberOfComponents(final GameTestHelper test) {
        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        if (0 != components.size())
            test.fail("Newly created component set should be empty");

        // Make sure changes affect the count
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        if (1 != components.size())
            test.fail("Expected a single component registered.");

        // Also make sure "other" components affect the count
        components.registerOther("O", new IRecipeComponent() {
            @Override
            public RecipeComponentType<?> getType() {
                return null;
            }
        });

        if (2 != components.size())
            test.fail("Expected 2 components registered after other type registration.");

        test.succeed();
    }
}
