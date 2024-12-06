package dev.compactmods.crafting.data;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.ActiveWorldFields;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public interface CCAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactCrafting.MOD_ID);

    DeferredHolder<AttachmentType<?>, AttachmentType<ActiveWorldFields>> ACTIVE_FIELDS = ATTACHMENT_TYPES
            .register("active_fields", () -> AttachmentType.builder((holder) -> ActiveWorldFields.create((Level) holder)).build());

    static <T> IAttachmentSerializer<Tag, T> holderWith(Codec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public @NotNull T read(@NotNull IAttachmentHolder holder, @NotNull Tag tag, HolderLookup.@NotNull Provider provider) {
                var parse = codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag);
                if (parse.error().isPresent()) {
                    throw new RuntimeException(parse.error().get().toString());
                }
                if (parse.result().isEmpty())
                    throw new RuntimeException("Result not present");

                var data = parse.result().get();
                setter.accept(data, holder);
                return data;
            }

            @Override
            public Tag write(@NotNull T attachment, HolderLookup.@NotNull Provider provider) {
                var encode = codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment);
                if (encode.error().isPresent()) {
                    throw new RuntimeException(encode.error().get().toString());
                }
                if (encode.result().isEmpty())
                    throw new RuntimeException("Result not present");

                return encode.result().get();
            }
        };
    }
}
