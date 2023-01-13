package dev.compactmods.crafting.tests.proxies;

import javax.annotation.Nonnull;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import dev.compactmods.crafting.tests.recipes.util.RecipeLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RescanProxyTests {

    @Nonnull
    private static RescanFieldProxyEntity setupProxyForMediumField(GameTestHelper test) {
        final BlockPos proxyLocation = new BlockPos(0, 3, 0);

        test.setBlock(proxyLocation, CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());
        test.setBlock(proxyLocation.below(), Blocks.BLACK_CONCRETE);

        if(test.getBlockEntity(proxyLocation) instanceof RescanFieldProxyEntity proxy) {
            BlockPos centerField = new BlockPos(6, 4, 6);
            proxy.updateField(test.relativePos(centerField));
            return proxy;
        } else {
            test.fail("Created block is not a proxy.");
            return null;
        }
    }

    @GameTest(template = "medium_field", required = false)
    public static void RescanProxyDoesNotRescanActivelyCraftingField(final GameTestHelper test) {

        RescanFieldProxyEntity proxy = setupProxyForMediumField(test);
//        test.addNamedRunnable("postMatch", () -> {
//            scheduler.thenRun(0, () -> {
//                // spawn catalyst item to drop into field
//                BlockPos aboveField = test.getOrigin().offset(new BlockPos(6, 7, 6));
//                ItemEntity catalyst = new ItemEntity(test.getWorld(),
//                        aboveField.getX(), aboveField.getY(), aboveField.getZ(),
//                        new ItemStack(Items.ENDER_PEARL));
//
//                test.getWorld().addFreshEntity(catalyst);
//            }).thenRun(10, () -> {
//                test.setBlockState(BlockPos.ZERO, Blocks.REDSTONE_BLOCK.defaultBlockState());
//            }).thenRun(1, () -> {
//                // after the rescan is triggered, the field should still be matched and crafting
//                proxy.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD).ifPresent(field -> {
//                    test.assertTrue(() -> field.getCurrentRecipe().isPresent(), "Recipe was cleared");
//                    test.assertTrue(() -> EnumCraftingState.CRAFTING.equals(field.getCraftingState()),
//                            "Field crafting state erroneously not 'CRAFTING'");
//                });
//            });
//        });
//
        RecipeLevelHelper.loadStructureIntoTestArea(
                test,
                new ResourceLocation("compactcrafting", "recipes/ender_crystal"),
                new BlockPos(4, 2, 4));

        test.pulseRedstone(proxy.getBlockPos().below().north(), 15);

        final LazyOptional<IMiniaturizationField> f = proxy.getCapability(CCCapabilities.MINIATURIZATION_FIELD);

        test.fail("wip");

//        f.ifPresent(field -> {
//            field.registerListener(LazyOptional.of(() -> new IFieldListener() {
//                @Override
//                public void onRecipeMatched(IMiniaturizationField field, IMiniaturizationRecipe recipe) {
//                    test.executeNamedRunnable("postMatch");
//                }
//            }));
//
//            field.fieldContentsChanged();
//        });
    }
}
