package dev.compactmods.crafting.tests.field;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class IMiniaturizationFieldTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void fresh_field_has_no_projectors(final GameTestHelper test) {
        IMiniaturizationField blank = new BlankMiniaturizationField();

        final Stream<BlockPos> positions = GameTestAssertions.assertDoesNotThrow(blank::getProjectorPositions);
        if (null == positions) {
            test.fail("Positions are null.");
            return;
        }

        if(positions.findAny().isPresent())
            test.fail("Positions should be empty.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void BasicClientDataNoRecipe(final GameTestHelper test) {
        IMiniaturizationField blank = new BlankMiniaturizationField();
        final CompoundTag clientData = GameTestAssertions.assertDoesNotThrow(blank::clientData);

        if (null == clientData)
            test.fail("Client data is null");

        test.succeed();
    }
}
