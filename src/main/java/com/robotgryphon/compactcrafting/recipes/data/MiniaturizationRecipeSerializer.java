package com.robotgryphon.compactcrafting.recipes.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.data.json.MiniaturizationRecipeJsonSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Optional;

public class MiniaturizationRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, JsonObject json) {
        Optional<MiniaturizationRecipe> attempt = MiniaturizationRecipeJsonSerializer.deserialize(json, recipeId);
        return attempt.orElse(null);
    }

    @Nullable
    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe(recipeId);

        CompoundNBT outputMeta = buffer.readCompoundTag();
        if (outputMeta.getInt("outputs") > 0) {
            int numOutputs = outputMeta.getInt("outputs");
            for (int out = 0; out < numOutputs; out++) {
                ItemStack output = buffer.readItemStack();
                recipe.addOutput(output);
            }
        }

        CompoundNBT components = buffer.readCompoundTag();
        int numComponents = components.getInt("count");
        if(numComponents > 0) {
            ListNBT compList = components.getList("components", Constants.NBT.TAG_COMPOUND);
            compList.forEach(comp -> {
                CompoundNBT compC = (CompoundNBT) comp;
                String key = compC.getString("key");
                CompoundNBT stateTag = compC.getCompound("state");

                BlockState.CODEC.decode(NBTDynamicOps.INSTANCE, stateTag)
                        .resultOrPartial(CompactCrafting.LOGGER::error)
                        .ifPresent(state -> {
                            BlockState compState = state.getFirst();
                            recipe.addComponent(key, compState);

                            CompactCrafting.LOGGER.debug("Got component: {} ({})", key, compState.toString());
                        });
            });
        }
        return recipe;
    }

    @Override
    public void write(PacketBuffer buffer, MiniaturizationRecipe recipe) {
        ItemStack[] outputs = recipe.getOutputs();

        CompoundNBT outputMeta = new CompoundNBT();
        outputMeta.putInt("outputs", outputs.length);
        buffer.writeCompoundTag(outputMeta);

        if (outputs.length > 0) {
            for (ItemStack out : outputs) buffer.writeItemStack(out);
        }

        try {
            CompoundNBT componentMeta = new CompoundNBT();
            int numComponents = recipe.getComponentKeys().size();
            componentMeta.putInt("count", numComponents);

            if (numComponents > 0) {
                ListNBT compList = new ListNBT();
                recipe.getComponents().forEach((key, state) -> {
                    DataResult<INBT> encode = BlockState.CODEC.encode(state, NBTDynamicOps.INSTANCE, null);
                    encode
                        .resultOrPartial(CompactCrafting.LOGGER::error)
                        .ifPresent(stateNbt -> {
                            CompoundNBT componentTag = new CompoundNBT();
                            componentTag.put("state", stateNbt);
                            componentTag.putString("key", key);

                            compList.add(componentTag);
                        });
                });

                componentMeta.put("components", compList);
            }

            buffer.writeCompoundTag(componentMeta);
        } catch (Exception ex) {
        }
    }
}
