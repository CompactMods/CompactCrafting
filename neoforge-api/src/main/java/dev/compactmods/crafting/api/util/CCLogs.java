package dev.compactmods.crafting.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CCLogs {
    Logger LOGGER = LogManager.getLogger("compactcrafting");
}
