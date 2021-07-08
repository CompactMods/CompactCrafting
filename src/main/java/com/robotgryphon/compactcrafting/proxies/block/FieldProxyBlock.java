package com.robotgryphon.compactcrafting.proxies.block;

import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FieldProxyBlock extends Block {
    public FieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return null;
    }

    @Override
    public void setPlacedBy(World level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        PlayerEntity player = null;
        if(entity instanceof PlayerEntity) {
            player = (PlayerEntity) entity;
        }

        if(stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();

            if(nbt.contains("field")) {
                CompoundNBT fieldData = nbt.getCompound("field");
                if(fieldData.contains("center")) {
                    BlockPos center = NBTUtil.readBlockPos(fieldData.getCompound("center"));

                    PlayerEntity finalPlayer = player;
                    level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                            .resolve()
                            .flatMap(fields -> fields.get(center))
                            .ifPresent(field -> {
                                if(finalPlayer != null) {
                                    finalPlayer.displayClientMessage(new StringTextComponent("linked to field"), true);
                                }
                            });
                }
            }
        }
    }
}
