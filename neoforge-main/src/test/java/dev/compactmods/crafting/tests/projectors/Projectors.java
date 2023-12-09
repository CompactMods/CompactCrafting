package dev.compactmods.crafting.tests.projectors;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(CompactCrafting.MOD_ID)
public class Projectors {

    @GameTest(template = "small_field")
    public static void CanPlaceProjector(final GameTestHelper test) {
        final BlockPos fieldCenter = MiniaturizationFieldSize.SMALL.getOriginCenterFromCorner()
                .above();

        final Set<BlockState> locatedStates = MiniaturizationFieldSize.SMALL.getProjectorLocations(fieldCenter)
                .map(test::getBlockState)
                .collect(Collectors.toSet());

        final Set<BlockState> projectors = locatedStates.stream()
                .filter(state -> state.getBlock() instanceof FieldProjectorBlock)
                .collect(Collectors.toSet());

        // Must have four projectors
        if (4 != projectors.size()) {
            test.fail("Expected 4 projectors; got " + projectors.size());
            return;
        }

        // Must have all four directions represented
        final Set<Direction> HORIZONTAL = Direction.Plane.HORIZONTAL.stream().collect(Collectors.toSet());
        final Set<Direction> directions = projectors.stream()
                .map(state -> state.getValue(FieldProjectorBlock.FACING))
                .collect(Collectors.toSet());

        if (!HORIZONTAL.equals(directions)) {
            test.fail("Expected projectors in specific locations.");
            return;
        }

        final var anyProjectorOff = locatedStates.stream()
                .filter(state -> state.getValue(FieldProjectorBlock.SIZE) != MiniaturizationFieldSize.SMALL)
                .findAny();

        if (anyProjectorOff.isPresent()) {
            test.fail("Expected all four projectors to be active and size SMALL.");
            return;
        }

        test.succeed();
    }

    @GameTest(template = "small_field")
    public static void GeneratesFieldCapabilityInstance(final GameTestHelper test) {
        final BlockPos center = test.absolutePos(MiniaturizationFieldSize.SMALL.getOriginCenterFromCorner().above());

        final ServerLevel level = test.getLevel();

        level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
            if (!fields.hasActiveField(center))
                test.fail("Expected field instance to exist.");

            try {
                final Optional<IMiniaturizationField> field = fields.get(center);
                if (field.isEmpty())
                    test.fail("Expected a field instance.");

                final IMiniaturizationField fieldInst = field.get();
                if(!fieldInst.getCenter().equals(center))
                    test.fail("Field centers do not match.");

                test.succeed();

            } catch (Exception e) {
                test.fail("Expected to fetch a field instance by its center; method threw.");
            }
        });
    }

    @GameTest(template = "empty_medium")
    public static void PlacingSingleActiveProjectorDeactivatesIt(final GameTestHelper test) {
        final BlockState stateSmall = CCBlocks.FIELD_PROJECTOR_BLOCK.get()
                .defaultBlockState()
                .setValue(FieldProjectorBlock.SIZE, MiniaturizationFieldSize.SMALL);

        test.setBlock(new BlockPos(1, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.NORTH));
        test.setBlock(new BlockPos(2, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.SOUTH));
        test.setBlock(new BlockPos(3, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.WEST));
        test.setBlock(new BlockPos(4, 1, 1), stateSmall.setValue(FieldProjectorBlock.FACING, Direction.EAST));

        final BlockState northState = test.getBlockState(new BlockPos(1, 1, 1));
        if (FieldProjectorBlock.isActive(northState))
            test.fail("North projector still active.");

        final BlockState southState = test.getBlockState(new BlockPos(2, 1, 1));
        if (FieldProjectorBlock.isActive(southState))
            test.fail("South projector still active.");

        final BlockState westState = test.getBlockState(new BlockPos(3, 1, 1));
        if (FieldProjectorBlock.isActive(westState))
            test.fail("West projector still active.");

        final BlockState eastState = test.getBlockState(new BlockPos(4, 1, 1));
        if (FieldProjectorBlock.isActive(eastState))
            test.fail("East projector still active.");

        test.succeed();
    }
}
