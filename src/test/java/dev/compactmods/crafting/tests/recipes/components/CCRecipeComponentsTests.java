package dev.compactmods.crafting.tests.recipes.components;

import java.util.Map;
import java.util.Optional;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class CCRecipeComponentsTests {

    @Test
    void CanCreateInstance() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        Assertions.assertNotNull(components);
    }

    @Test
    void CanFetchComponentsFresh() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
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

    @Test
    @Tag("minecraft")
    void CanRegisterAndFetchBlocks() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        final BlockComponent BLOCK_COMPONENT = new BlockComponent(Blocks.GOLD_BLOCK);
        components.registerBlock("G", BLOCK_COMPONENT);

        Assertions.assertTrue(components.hasBlock("G"), "Block not registered.");

        final Optional<IRecipeBlockComponent> block = components.getBlock("G");
        Assertions.assertTrue(block.isPresent());

        block.ifPresent(comp -> {
            // If the component is found, make sure it matches our original
            Assertions.assertEquals(BLOCK_COMPONENT, comp);

            // Also check the default block state, since no filters are applied
            Assertions.assertTrue(comp.matches(Blocks.GOLD_BLOCK.defaultBlockState()));
        });
    }

    @Test
    @Tag("minecraft")
    void EmptyBlocksAreActuallyEmpty() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        components.registerBlock("E", new EmptyBlockComponent());

        Assertions.assertFalse(components.isEmptyBlock("G"), "gold block should not be empty");
        Assertions.assertTrue(components.isEmptyBlock("E"), "empty block not actually empty");
    }

    @Test
    void UnregisteredBlocksAreConsideredEmpty() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        Assertions.assertTrue(components.isEmptyBlock("A"), "Unregistered block not considered empty");

        final Optional<IRecipeBlockComponent> u = components.getBlock("U");
        u.ifPresent(b -> Assertions.fail("Unregistered block is present."));

        Assertions.assertEquals(Optional.empty(), u);
    }

    @Test
    void CanRegisterNonBlockComponents() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
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
    @Tag("minecraft")
    void CanGetNumberOfComponents() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        Assertions.assertEquals(0, components.size());

        // Make sure changes affect the count
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));
        Assertions.assertEquals(1, components.size());

        // Also make sure "other" components affect the count
        components.registerOther("O", new IRecipeComponent() {
            @Override
            public RecipeComponentType<?> getType() {
                return null;
            }
        });
        Assertions.assertEquals(2, components.size());
    }
}
