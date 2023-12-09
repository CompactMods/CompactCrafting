package dev.compactmods.crafting.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class CodecExtensions {

     public static final Codec<Block> BLOCK_ID_CODEC = ResourceLocation.CODEC
             .flatXmap(rl -> BuiltInRegistries.BLOCK.containsKey(rl) ?
                             DataResult.success(BuiltInRegistries.BLOCK.get(rl)) :
                             DataResult.error(() -> String.format("Block %s is not registered.", rl)),
                bl -> DataResult.success(BuiltInRegistries.BLOCK.getKey(bl)))
             .stable();

     /**
      * Variant of the ItemStack codec that allows for some optional defaults such as a default
      * stack size of 1.
      */
     public static final Codec<ItemStack> FRIENDLY_ITEMSTACK = RecordCodecBuilder.create(i -> i.group(
             BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStack::getItem),
             Codec.INT.optionalFieldOf("Count", 1).forGetter(ItemStack::getCount),
             CompoundTag.CODEC.optionalFieldOf("tag")
                     .forGetter(is -> Optional.ofNullable(is.getTag()))
     ).apply(i, (id, count, tag) -> {
          final var is = new ItemStack(id, count);
          tag.ifPresent(is::setTag);
          return is;
     }));
}
