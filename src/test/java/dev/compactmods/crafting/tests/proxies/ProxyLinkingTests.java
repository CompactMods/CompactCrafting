package dev.compactmods.crafting.tests.proxies;

import javax.annotation.Nonnull;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("proxies")
public class ProxyLinkingTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @IntegrationTest("empty_medium")
    void CanPlaceProxy(IntegrationTestHelper test) {
        test.setBlockState(new BlockPos(2, 0, 2), Registration.MATCH_FIELD_PROXY_BLOCK.get().defaultBlockState());
        test.setBlockState(new BlockPos(2, 0, 4), Registration.RESCAN_FIELD_PROXY_BLOCK.get().defaultBlockState());
    }

    @IntegrationTest("medium_field")
    void CanLinkProxy(IntegrationTestHelper test) {
        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        final LazyOptional<IMiniaturizationField> cap = proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);

        Assertions.assertTrue(cap.isPresent());
    }

    @IntegrationTest("medium_field")
    void MatchProxyPowersOnForRecipeMatch(IntegrationTestHelper test) {
        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        // Set up a deferred check, so we can schedule a block check after the recipe changes
        test.addNamedRunnable("postRecipeMatch", () -> {
            final IntegrationTestHelper.ScheduleHelper scheduler = test.scheduler();
            scheduler.thenRun(5, () -> {
                final Boolean lit = test.getBlockState(BlockPos.ZERO).getValue(RedstoneLampBlock.LIT);
                Assertions.assertTrue(lit);
            }).thenWait(5);
        });

        RecipeTestUtil.loadStructureIntoTestArea(test,
                new ResourceLocation("compactcrafting", "recipes/ender_crystal"),
                new BlockPos(4, 1, 4));

        proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                .ifPresent(field -> {
                    field.registerListener(LazyOptional.of(() -> new IFieldListener() {
                        @Override
                        public void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {
                            Assertions.assertNotNull(recipe);
                            test.executeNamedRunnable("postRecipeMatch");
                        }
                    }));

                    field.fieldContentsChanged();
                });
    }

    @IntegrationTest("medium_field")
    void ProxyDisconnectsIfFieldDestabilizes(IntegrationTestHelper test) {
        BlockPos relFieldCenter = new BlockPos(6, 3, 6);

        MatchFieldProxyEntity proxy = setupProxyForMediumField(test);

        final LazyOptional<IMiniaturizationField> fieldRef = proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);
        fieldRef.resolve();

        // Ensure proxy is connected to field
        Assertions.assertTrue(fieldRef.isPresent());

        test.addNamedRunnable("postBreak", () -> {
            final LazyOptional<IMiniaturizationField> fieldPostBreak = proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);
            fieldPostBreak.resolve();

            Assertions.assertFalse(fieldPostBreak.isPresent());
        });

        BlockPos relNorthProjector = MiniaturizationFieldSize.MEDIUM.getProjectorLocationForDirection(relFieldCenter, Direction.NORTH);
        test.destroyBlock(relNorthProjector, false);

        test.scheduler().thenRun(5, () -> test.executeNamedRunnable("postBreak"));
    }


    @Nonnull
    private MatchFieldProxyEntity setupProxyForMediumField(IntegrationTestHelper test) {
        final BlockPos proxyLocation = new BlockPos(0, 1, 0);

        test.setBlockState(proxyLocation, Registration.MATCH_FIELD_PROXY_BLOCK.get().defaultBlockState());
        test.setBlockState(BlockPos.ZERO, Blocks.REDSTONE_LAMP.defaultBlockState());

        final TileEntity tile = test.getTileEntity(proxyLocation);
        Assertions.assertTrue(tile instanceof MatchFieldProxyEntity);

        MatchFieldProxyEntity proxy = (MatchFieldProxyEntity) tile;

        BlockPos centerField = new BlockPos(6, 3, 6);
        proxy.updateField(test.relativePos(centerField).orElse(BlockPos.ZERO));
        return proxy;
    }
}
