package dev.compactmods.crafting.api.capability;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.projector.capability.FieldProjectorController;
import net.neoforged.neoforge.capabilities.BlockCapability;

public interface FieldProjectorCapabilities {

    BlockCapability<FieldProjectorController, Void> FIELD_PROJECTOR_CONTROL = BlockCapability.createVoid(
            CompactCrafting.identifier("field_projector"),
            FieldProjectorController.class
    );

}
