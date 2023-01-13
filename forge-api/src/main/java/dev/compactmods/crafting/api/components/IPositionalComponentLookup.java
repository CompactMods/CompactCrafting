package dev.compactmods.crafting.api.components;

import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface IPositionalComponentLookup {

    Collection<String> getComponents();

    void add(BlockPos location, String component);
    void remove(String component);

    Stream<BlockPos> getAllPositions();
    Stream<BlockPos> getPositionsForComponent(String component);
    Optional<String> getRequiredComponentKeyForPosition(BlockPos pos);

    boolean containsLocation(BlockPos location);
}
