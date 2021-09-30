package dev.compactmods.crafting.tests.api;

import java.util.stream.Stream;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.api.field.BlankMiniaturizationField;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class IMiniaturizationFieldTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Test
    @Tag("minecraft")
    void DefaultProjectorLocationsEmpty() {
        IMiniaturizationField blank = new BlankMiniaturizationField();

        final Stream<BlockPos> positions = Assertions.assertDoesNotThrow(blank::getProjectorPositions);
        Assertions.assertNotNull(positions);
        Assertions.assertEquals(0, positions.count());
    }

    @Test
    @Tag("minecraft")
    void BasicClientDataNoRecipe() {
        IMiniaturizationField blank = new BlankMiniaturizationField();
        final CompoundNBT clientData = Assertions.assertDoesNotThrow(blank::clientData);

        Assertions.assertNotNull(clientData);
    }
}
