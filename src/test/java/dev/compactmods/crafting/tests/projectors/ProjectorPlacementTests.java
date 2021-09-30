package dev.compactmods.crafting.tests.projectors;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("projectors")
public class ProjectorPlacementTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @IntegrationTest("small_field")
    void CanPlaceProjector(IntegrationTestHelper test) {
        final MutableBoundingBox testBounds = test.getTestBounds();

        final BlockPos fieldCenter = MiniaturizationFieldSize.SMALL.getOriginCenterFromCorner();

        final Set<BlockState> locatedStates = MiniaturizationFieldSize.SMALL.getProjectorLocations(fieldCenter)
                .map(test::getBlockState)
                .collect(Collectors.toSet());

        final Set<BlockState> projectors = locatedStates.stream()
                .filter(state -> state.getBlock() instanceof FieldProjectorBlock)
                .collect(Collectors.toSet());

        // Must have four projectors
        Assertions.assertEquals(4, projectors.size());

        // Must have all four directions represented
        final Set<Direction> HORIZONTAL = Direction.Plane.HORIZONTAL.stream().collect(Collectors.toSet());
        final Set<Direction> directions = projectors.stream()
                .map(state -> state.getValue(FieldProjectorBlock.FACING))
                .collect(Collectors.toSet());

        Assertions.assertEquals(HORIZONTAL, directions);

        final boolean anyProjectorOff = locatedStates.stream()
                .anyMatch(state -> state.getValue(FieldProjectorBlock.SIZE) != MiniaturizationFieldSize.SMALL);

        Assertions.assertFalse(anyProjectorOff, "Expected all four projectors to be active and size SMALL.");
    }

    @IntegrationTest("small_field")
    void GeneratesFieldCapabilityInstance(IntegrationTestHelper test) {
        final BlockPos center = MiniaturizationFieldSize.SMALL.getOriginCenterFromCorner().offset(test.getOrigin());

        final ServerWorld level = test.getWorld();

        level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS).ifPresent(fields -> {
            Assertions.assertTrue(fields.hasActiveField(center), "Expected field instance to exist.");

            final Optional<IMiniaturizationField> field = Assertions.assertDoesNotThrow(() -> fields.get(center));
            Assertions.assertTrue(field.isPresent());

            final IMiniaturizationField fieldInst = field.get();
            Assertions.assertEquals(center, fieldInst.getCenter());
        });
    }

    @IntegrationTest("empty_medium")
    void PlacingSingleActiveProjectorDeactivatesIt(IntegrationTestHelper test) {
        final BlockState stateSmall = Registration.FIELD_PROJECTOR_BLOCK.get()
                .defaultBlockState()
                .setValue(FieldProjectorBlock.SIZE, MiniaturizationFieldSize.SMALL);

        test.setBlockState(new BlockPos(1, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.NORTH));
        test.setBlockState(new BlockPos(2, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.SOUTH));
        test.setBlockState(new BlockPos(3, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.WEST));
        test.setBlockState(new BlockPos(4, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.EAST));

        final BlockState northState = test.getBlockState(new BlockPos(1, 1, 1));
        Assertions.assertFalse(FieldProjectorBlock.isActive(northState));

        final BlockState southState = test.getBlockState(new BlockPos(2, 1, 1));
        Assertions.assertFalse(FieldProjectorBlock.isActive(southState));

        final BlockState westState = test.getBlockState(new BlockPos(3, 1, 1));
        Assertions.assertFalse(FieldProjectorBlock.isActive(westState));

        final BlockState eastState = test.getBlockState(new BlockPos(4, 1, 1));
        Assertions.assertFalse(FieldProjectorBlock.isActive(eastState));
    }

//    @IntegrationTest("empty_medium")
//    void FailureTest(IntegrationTestHelper test) {
//        Assertions.fail("Uncomment in production.");
//    }
}
