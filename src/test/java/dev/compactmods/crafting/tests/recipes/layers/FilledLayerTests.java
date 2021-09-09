package dev.compactmods.crafting.tests.recipes.layers;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class FilledLayerTests {

    private FilledComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile(filename);

        return FilledComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow(false, Assertions::fail);
    }

    @Test
    @Tag("minecraft")
    void CanCreateLayerInstance() {
        getLayerFromFile("layers/filled/basic.json");
    }

    @Test
    @Tag("minecraft")
    void ReturnsNoFilledIfDimensionsNull() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");

        int filled = Assertions.assertDoesNotThrow(layer::getNumberFilledPositions);
        Assertions.assertEquals(0, filled);
    }

    @Test
    @Tag("minecraft")
    void CanUpdateDimensions() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");

        int filledBefore = layer.getNumberFilledPositions();

        AxisAlignedBB newDims = new AxisAlignedBB(0, 0, 0, 5, 1, 5);
        Assertions.assertDoesNotThrow(() -> layer.setRecipeDimensions(newDims));

        int filledAfter = layer.getNumberFilledPositions();

        Assertions.assertNotEquals(filledBefore, filledAfter, "Expected component count to change after growing layer dimensions.");
    }


}
