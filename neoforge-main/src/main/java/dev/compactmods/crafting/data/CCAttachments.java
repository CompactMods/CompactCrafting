package dev.compactmods.crafting.data;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.ActiveWorldFields;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public interface CCAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactCrafting.MOD_ID);

    DeferredHolder<AttachmentType<?>, AttachmentType<ActiveWorldFields>> ACTIVE_FIELDS = ATTACHMENT_TYPES
            .register("active_fields", () -> AttachmentType.builder((holder) -> ActiveWorldFields.create((Level) holder))
                    .serialize(new ActiveWorldFieldsSerializer())
                    .build());
    
    class ActiveWorldFieldsSerializer implements IAttachmentSerializer<Tag, ActiveWorldFields> {
        @Override
        public @NotNull ActiveWorldFields read(@NotNull IAttachmentHolder holder, @NotNull Tag tag, HolderLookup.@NotNull Provider provider) {
            Level level = (Level) holder;
            ActiveWorldFields fields = ActiveWorldFields.create(level);
            if (tag instanceof ListTag listTag) {
                fields.deserializeNBT(listTag);
            }
            return fields;
        }

        @Override
        public Tag write(@NotNull ActiveWorldFields attachment, HolderLookup.@NotNull Provider provider) {
            return attachment.serializeNBT();
        }
    }
}
