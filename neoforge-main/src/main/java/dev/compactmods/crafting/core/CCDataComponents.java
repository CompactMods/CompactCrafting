package dev.compactmods.crafting.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CCDataComponents {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS = 
            DeferredRegister.createDataComponents(CompactCrafting.MOD_ID);
    
    public record FieldCenter(BlockPos center) {
    }
    
    public static final Codec<FieldCenter> FIELD_CENTER_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("center").forGetter(FieldCenter::center)
            ).apply(instance, FieldCenter::new)
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, FieldCenter> FIELD_CENTER_STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, FieldCenter::center,
                    FieldCenter::new
            );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FieldCenter>> FIELD_CENTER = 
            DATA_COMPONENTS.registerComponentType(
                    "field_center",
                    builder -> builder
                            .persistent(FIELD_CENTER_CODEC)
                            .networkSynchronized(FIELD_CENTER_STREAM_CODEC)
            );
    
    public static void init(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}