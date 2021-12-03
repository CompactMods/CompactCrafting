package dev.compactmods.crafting.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class CodecExtensions {

     public static final Codec<Block> BLOCK_ID_CODEC = ResourceLocation.CODEC
             .flatXmap(rl -> ForgeRegistries.BLOCKS.containsKey(rl) ?
                             DataResult.success(ForgeRegistries.BLOCKS.getValue(rl)) :
                             DataResult.error(String.format("Block %s is not registered.", rl)),
                bl -> DataResult.success(bl.getRegistryName()))
             .stable();
}
