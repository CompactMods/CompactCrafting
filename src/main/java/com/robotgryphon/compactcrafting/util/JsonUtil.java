package com.robotgryphon.compactcrafting.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class JsonUtil {
    private JsonUtil() {}

    public static Optional<ItemStack> getItemStack(JsonObject json) {
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, json)
                .get()
                .ifRight(err -> CompactCrafting.LOGGER.warn("Failed to load itemstack from JSON: {}", err.message()))
                .mapLeft(Pair::getFirst)
                .left();
    }

    public static Optional<BlockState> getBlockState(JsonObject json) {
        Either<
                Pair<BlockState, JsonElement>,
                DataResult.PartialResult<Pair<BlockState, JsonElement>>
                > either = BlockState.CODEC.decode(JsonOps.INSTANCE, json).get();

        Optional<DataResult.PartialResult<Pair<BlockState, JsonElement>>> partial = either.right();
        if(partial.isPresent()) {
            String error = partial.get().message();
            ;

            DataResult.PartialResult<BlockState> result = partial.get().map(b -> b.getFirst().getBlockState());
            return Optional.empty();
        }

        return either.mapLeft(Pair::getFirst).left();
    }

    public static Optional<RecipeBlockStateComponent> getPossibleStates(JsonObject root) {
        // Get block information for node
        ResourceLocation blockId = new ResourceLocation(root.get("Name").getAsString());
        if(!ForgeRegistries.BLOCKS.containsKey(blockId))
            return Optional.empty();

        Block b = ForgeRegistries.BLOCKS.getValue(blockId);
        if(b == null)
            return Optional.empty();

        RecipeBlockStateComponent matcher = new RecipeBlockStateComponent(b);
        if(!root.has("Properties")) {
            // Save block itself and mark that all properties are valid
            return Optional.of(matcher);
        }

        // Get state information for block
        StateContainer<Block, BlockState> states = b.getStateDefinition();

        JsonObject properties = root.get("Properties").getAsJsonObject();

        properties.entrySet().forEach(jsonProperty -> {
            String propertyName = jsonProperty.getKey();

            // Get actual state property from state container on block
            Property<?> stateProperty = states.getProperty(propertyName);
            if (stateProperty == null)
                return;

            JsonElement valueRaw = jsonProperty.getValue();
            if(valueRaw.isJsonObject()) {
                JsonObject propMap = valueRaw.getAsJsonObject();

                // Need to specify property mapping type -- ALLOW, DISALLOW, MATCH
                if(!propMap.has("type")) {
                    return;
                }

                String propMapType = propMap.get("type").getAsString();
                switch(propMapType.toLowerCase(Locale.ROOT)) {
                    case "allow":
                        if(!propMap.has("values")) {
                            CompactCrafting.LOGGER.warn("No value specified for property {}", propertyName);
                        }

                        Predicate<Comparable<?>> pred = (val) -> true;
                        for (JsonElement acceptedValue : propMap.get("values").getAsJsonArray()) {
                            if (!acceptedValue.isJsonPrimitive()) continue;
                            JsonPrimitive prim = acceptedValue.getAsJsonPrimitive();
                            if (!prim.isString()) continue;
                            Optional<?> parsed = stateProperty.getValue(prim.getAsString());
                            if (!parsed.isPresent()) continue;
                            pred = pred.and((stateValue) -> stateValue.equals(parsed.get()));
                        }

                        matcher.setFilter(propertyName, pred);
                        break;

                    case "disallow":
                        break;

                    case "match":
                        if(propMap.has("value")) {
                            String propValue = propMap.get("value").getAsString();
                            Optional<?> parsed = stateProperty.getValue(propValue);

                            if(!parsed.isPresent()) {
                                CompactCrafting.LOGGER.warn("Value for {} is invalid. Allowed values: {}", propertyName, stateProperty.getPossibleValues());
                                return;
                            }

                            parsed.ifPresent(v -> {
                                matcher.setFilter(propertyName, (stateValue) -> stateValue.equals(v));
                            });
                        } else {
                            CompactCrafting.LOGGER.warn("No value specified for property {}", propertyName);
                        }
                        break;
                }
            }
        });

        return Optional.of(matcher);
    }
}
