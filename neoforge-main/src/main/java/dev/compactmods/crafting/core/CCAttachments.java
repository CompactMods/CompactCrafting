package dev.compactmods.crafting.core;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import dev.compactmods.crafting.field.ActiveWorldFields;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface CCAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactCrafting.MOD_ID);

    DeferredHolder<AttachmentType<?>, AttachmentType<ActiveWorldFields>> ACTIVE_FIELDS = ATTACHMENT_TYPES
            .register("active_fields", () -> AttachmentType.builder((holder) -> ActiveWorldFields.create((Level) holder)).build());

    DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PLACEMENT_TIMER = ATTACHMENT_TYPES
            .register("placement_timer", () -> AttachmentType.builder(() -> ClientConfig.placementTime).build());

    DeferredHolder<AttachmentType<?>, AttachmentType<ObjectArrayList<GhostProjectorPlacementRenderer>>> PLACEMENT_HELPERS = ATTACHMENT_TYPES
            .register("placement_helpers", () -> AttachmentType
                    .builder(() -> new ObjectArrayList<GhostProjectorPlacementRenderer>())
                    .build());
}
