package dev.compactmods.crafting.tests.field;

import java.util.stream.Stream;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IMiniaturizationFieldTests {

    @Test
    void DefaultProjectorLocationsEmpty() {
        IMiniaturizationField blank = new BlankMiniaturizationField();

        final Stream<BlockPos> positions = Assertions.assertDoesNotThrow(blank::getProjectorPositions);
        Assertions.assertNotNull(positions);
        Assertions.assertEquals(0, positions.count());
    }

    @Test
    void BasicClientDataNoRecipe() {
        IMiniaturizationField blank = new BlankMiniaturizationField();
        final CompoundTag clientData = Assertions.assertDoesNotThrow(blank::clientData);

        Assertions.assertNotNull(clientData);
    }
}
