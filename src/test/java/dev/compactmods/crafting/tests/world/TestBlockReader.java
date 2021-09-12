package dev.compactmods.crafting.tests.world;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class TestBlockReader implements IBlockReader {

    AxisAlignedBB knownBounds;
    final Map<BlockPos, BlockState> states;

    private TestBlockReader() {
        states = new HashMap<>();
    }

    public static TestBlockReader fromRecipe(MiniaturizationRecipe recipe) {
        TestBlockReader reader = new TestBlockReader();
        reader.knownBounds = recipe.getDimensions();

        int layeri = 0;
        for(IRecipeLayer layer : recipe.getLayers().collect(Collectors.toSet())) {
            for(String ck : layer.getComponents()) {
                BlockState relState = recipe.getComponents().getBlock(ck)
                        .map(c -> {
                            if (c instanceof BlockComponent) {
                                return ((BlockComponent) c).getFirstMatch().orElse(c.getRenderState());
                            } else {
                                return c.getRenderState();
                            }
                        })
                        .orElse(Blocks.AIR.defaultBlockState());

                int finalLayeri = layeri;
                layer.getPositionsForComponent(ck)
                        .map(p -> p.above(finalLayeri))
                        .forEach(p -> reader.states.put(p.immutable(), relState));
            }

            layeri++;
        }

        return reader;
    }

    public void setBounds(AxisAlignedBB bounds) {
        this.knownBounds = bounds;
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPos p_175625_1_) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!knownBounds.contains(pos.getX(), pos.getY(), pos.getZ()))
            return Blocks.AIR.defaultBlockState();

        if (!states.containsKey(pos))
            return Blocks.AIR.defaultBlockState();

        return states.get(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos p_204610_1_) {
        return Fluids.EMPTY.defaultFluidState();
    }

    public void replaceBlock(BlockPos position, BlockState state) {
        states.put(position, state);
    }
}
