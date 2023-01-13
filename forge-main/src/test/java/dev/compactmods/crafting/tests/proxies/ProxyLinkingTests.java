package dev.compactmods.crafting.tests.proxies;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import dev.compactmods.crafting.tests.recipes.util.RecipeLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nonnull;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ProxyLinkingTests {

    @GameTest(template = "empty_medium")
    public static void CanPlaceProxy(GameTestHelper test) {
        test.setBlock(new BlockPos(2, 0, 2), CCBlocks.MATCH_FIELD_PROXY_BLOCK.get().defaultBlockState());
        test.setBlock(new BlockPos(2, 0, 4), CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get().defaultBlockState());

        test.succeed();
    }

    @GameTest(template = "medium_field")
    public static void CanLinkProxy(GameTestHelper test) {
        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        final var cap = proxy.getCapability(CCCapabilities.MINIATURIZATION_FIELD);

        if (!cap.isPresent())
            test.fail("Expected proxy to have a field reference.");

        test.succeed();
    }

    @GameTest(template = "medium_field")
    public static void MatchProxyPowersOnForRecipeMatch(GameTestHelper test) {
        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        // Set up a deferred check, so we can schedule a block check after the recipe changes
        test.succeedWhen(() -> {
            var state = test.getBlockState(proxy.getBlockPos().below());
            if(state.getBlock() instanceof RedstoneLampBlock) {
                var lit = state.getValue(RedstoneLampBlock.LIT);
                if (lit) test.succeed();
            }
        });

        RecipeLevelHelper.loadStructureIntoTestArea(
                test,
                new ResourceLocation("compactcrafting", "recipes/ender_crystal"),
                new BlockPos(4, 1, 4));

        proxy.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                .ifPresent(IMiniaturizationField::fieldContentsChanged);
    }

    @GameTest(template = "medium_field")
    public static void ProxyDisconnectsIfFieldDestabilizes(GameTestHelper test) {
        BlockPos relFieldCenter = new BlockPos(6, 4, 6);

        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        final LazyOptional<IMiniaturizationField> fieldRef = proxy.getCapability(CCCapabilities.MINIATURIZATION_FIELD);

        // Ensure proxy is connected to field
        if (!fieldRef.isPresent())
            test.fail("Expected a field reference to be present.");

        BlockPos relNorthProjector = MiniaturizationFieldSize.MEDIUM.getProjectorLocationForDirection(relFieldCenter, Direction.NORTH);
        test.destroyBlock(relNorthProjector);

        test.runAfterDelay(5, () -> {
            final LazyOptional<IMiniaturizationField> fieldPostBreak = proxy.getCapability(CCCapabilities.MINIATURIZATION_FIELD);
            if (fieldPostBreak.isPresent())
                test.fail("Field capability instance is still available after the field destabilized.");

            test.assertBlockProperty(test.relativePos(proxy.getBlockPos().below()), RedstoneLampBlock.LIT, false);

            test.succeed();
        });
    }

    @Nonnull
    private static MatchFieldProxyEntity setupProxyForMediumField(GameTestHelper test) {
        var size = MiniaturizationFieldSize.MEDIUM;
        var offset = size.getProjectorDistance();

        final BlockPos proxyLocation = new BlockPos(0, 3, 0);
        final BlockPos fieldCenter = test.absolutePos(BlockPos.ZERO.offset(offset + 1, 4, offset + 1));

        test.setBlock(proxyLocation, CCBlocks.MATCH_FIELD_PROXY_BLOCK.get().defaultBlockState());
        test.setBlock(proxyLocation.below(), Blocks.REDSTONE_LAMP.defaultBlockState());

        final BlockEntity tile = test.getBlockEntity(proxyLocation);
        if (tile instanceof MatchFieldProxyEntity proxy) {
            proxy.updateField(fieldCenter);
            return proxy;
        }

        test.fail("Failed to set up match proxy.");
        return null;
    }
}
