package dev.compactmods.crafting.api.components;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface IPositionalComponentLookup {

    Collection<String> getComponents();

    IPositionalComponentLookup add(BlockPos location, String component);
    void remove(String component);

    Stream<BlockPos> getAllPositions();
    Stream<BlockPos> getPositionsForComponent(String component);
    Optional<String> getRequiredComponentKeyForPosition(BlockPos pos);

    boolean containsLocation(BlockPos location);

    void setFootprint(int xSize, int zSize);
    BoundingBox footprint();
}