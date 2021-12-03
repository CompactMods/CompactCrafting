package dev.compactmods.crafting.tests.proxies;

import javax.annotation.Nonnull;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("proxies")
public class RescanProxyTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    @Nonnull
    private RescanFieldProxyEntity setupProxyForMediumField(IntegrationTestHelper test) {
        final BlockPos proxyLocation = new BlockPos(0, 1, 0);

        test.setBlockState(proxyLocation, Registration.RESCAN_FIELD_PROXY_BLOCK.get().defaultBlockState());
        test.setBlockState(BlockPos.ZERO, Blocks.BLACK_CONCRETE.defaultBlockState());

        final TileEntity tile = test.getTileEntity(proxyLocation);
        Assertions.assertTrue(tile instanceof RescanFieldProxyEntity);

        RescanFieldProxyEntity proxy = (RescanFieldProxyEntity) tile;

        BlockPos centerField = new BlockPos(6, 3, 6);
        proxy.updateField(test.relativePos(centerField).orElse(BlockPos.ZERO));
        return proxy;
    }

    @IntegrationTest("medium_field")
    void RescanProxyDoesNotRescanActivelyCraftingField(IntegrationTestHelper test) {

        final IntegrationTestHelper.ScheduleHelper scheduler = test.scheduler();

        RescanFieldProxyEntity proxy = setupProxyForMediumField(test);
        test.addNamedRunnable("postMatch", () -> {
            scheduler.thenRun(0, () -> {
                // spawn catalyst item to drop into field
                BlockPos aboveField = test.getOrigin().offset(new BlockPos(6, 7, 6));
                ItemEntity catalyst = new ItemEntity(test.getWorld(),
                        aboveField.getX(), aboveField.getY(), aboveField.getZ(),
                        new ItemStack(Items.ENDER_PEARL));

                test.getWorld().addFreshEntity(catalyst);
            }).thenRun(10, () -> {
                test.setBlockState(BlockPos.ZERO, Blocks.REDSTONE_BLOCK.defaultBlockState());
            }).thenRun(1, () -> {
                // after the rescan is triggered, the field should still be matched and crafting
                proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD).ifPresent(field -> {
                    test.assertTrue(() -> field.getCurrentRecipe().isPresent(), "Recipe was cleared");
                    test.assertTrue(() -> EnumCraftingState.CRAFTING.equals(field.getCraftingState()),
                            "Field crafting state erroneously not 'CRAFTING'");
                });
            });
        });

        RecipeTestUtil.loadStructureIntoTestArea(test,
                new ResourceLocation("compactcrafting", "recipes/ender_crystal"),
                new BlockPos(4, 1, 4));

        final LazyOptional<IMiniaturizationField> f = proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD);
        f.ifPresent(field -> {
            field.registerListener(LazyOptional.of(() -> new IFieldListener() {
                @Override
                public void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {
                    test.executeNamedRunnable("postMatch");
                }
            }));

            field.fieldContentsChanged();
        });
    }
}
