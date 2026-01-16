package dev.compactmods.crafting.data;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.ActiveWorldFields;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface CCAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactCrafting.MOD_ID);

    DeferredHolder<AttachmentType<?>, AttachmentType<ActiveWorldFields>> ACTIVE_FIELDS = ATTACHMENT_TYPES
            .register("active_fields", () -> AttachmentType.builder((holder) -> ActiveWorldFields.create((Level) holder)).build());

}
