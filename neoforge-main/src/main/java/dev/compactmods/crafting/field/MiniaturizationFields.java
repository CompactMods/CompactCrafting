package dev.compactmods.crafting.field;

import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.field.impl.ActiveWorldFields;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface MiniaturizationFields {
    DeferredHolder<AttachmentType<?>, AttachmentType<ActiveWorldFields>> ACTIVE_FIELDS = CompactCraftingCommon.ATTACHMENT_TYPES
            .register("active_fields", () -> AttachmentType.builder((holder) -> new ActiveWorldFields((Level) holder)).build());

    static void prepare() {

    }
}
