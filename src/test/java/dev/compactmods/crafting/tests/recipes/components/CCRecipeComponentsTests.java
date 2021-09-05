package dev.compactmods.crafting.tests.recipes.components;

import java.util.Map;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CCRecipeComponentsTests {

    @Test
    void CanCreateInstance() {
        CCMiniRecipeComponents components = new CCMiniRecipeComponents();
        Assertions.assertNotNull(components);
    }

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
}
