package com.robotgryphon.compactcrafting.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class CodecExtensions {

     public static final Codec<Block> BLOCK_ID_CODEC = ResourceLocation.CODEC
             .flatXmap(rl -> ForgeRegistries.BLOCKS.containsKey(rl) ?
                             DataResult.success(ForgeRegistries.BLOCKS.getValue(rl)) :
                             DataResult.error(String.format("Block %s is not registered.", rl)),
                bl -> DataResult.success(bl.getRegistryName()))
             .stable();
}
