package com.robotgryphon.compactcrafting.recipes.data.util;

import com.mojang.serialization.DataResult;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public abstract class RecipeBufferDataUtil {

    public static final ResourceLocation TYPE_CATALYSTS = new ResourceLocation(CompactCrafting.MOD_ID, "catalysts");

    /**
     * Reads component information from a packet buffer, adding it to a recipe.
     *
     * @param recipe Recipe to add component information to.
     * @param buffer Packet to read component information from.
     */
    public static void readComponentInfo(MiniaturizationRecipe recipe, PacketBuffer buffer) {
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
    }

    /**
     * Writes component information for a recipe to a compound NBT tag.
     * @param recipe
     * @return
     */
    @Nonnull
    public static void writeComponentInfo(MiniaturizationRecipe recipe, PacketBuffer buffer) {
        CompoundNBT componentData = new CompoundNBT();
        int numComponents = recipe.getComponentKeys().size();
        componentData.putInt("count", numComponents);

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

            componentData.put("components", compList);
        }

        buffer.writeCompoundTag(componentData);
    }

    /**
     * Reads recipe output information from a packet buffer.
     *
     * @param recipe Recipe to load output information into.
     * @param buffer Buffer to pull data from.
     */
    public static void readRecipeOutputs(MiniaturizationRecipe recipe, PacketBuffer buffer) throws Exception {
        CompoundNBT outputMeta = buffer.readCompoundTag();
        if(outputMeta == null || outputMeta.isEmpty() || !outputMeta.contains("outputs"))
            throw new Exception("Output information is not readable: no output count or compound not readable.");

        if (outputMeta.getInt("outputs") > 0) {
            int numOutputs = outputMeta.getInt("outputs");
            for (int out = 0; out < numOutputs; out++) {
                ItemStack output = buffer.readItemStack();
                recipe.addOutput(output);
            }
        }
    }

    /**
     * Writes recipe output information to a packet buffer.
     *
     * @param recipe Recipe to write output information for.
     * @param buffer Buffer to write data to.
     */
    public static void writeRecipeOutputs(MiniaturizationRecipe recipe, PacketBuffer buffer) {
        ItemStack[] outputs = recipe.getOutputs();

        CompoundNBT outputMeta = new CompoundNBT();
        outputMeta.putInt("outputs", outputs.length);
        buffer.writeCompoundTag(outputMeta);

        if (outputs.length > 0) {
            for (ItemStack out : outputs) buffer.writeItemStack(out);
        }
    }

    /**
     * Reads catalyst information from a packet buffer.
     *
     * @param recipe The recipe to add catalyst information to.
     * @param buffer The buffer to read information from.
     * @throws Exception
     */
    public static void readRecipeCatalysts(MiniaturizationRecipe recipe, PacketBuffer buffer) throws Exception {
        CompoundNBT tag = buffer.readCompoundTag();
        if(tag == null || tag.isEmpty() || !tag.contains("type"))
            throw new Exception("Tag information is not readable: no type tag or compound not readable.");

        if(!tag.getString("type").equals(TYPE_CATALYSTS.toString()))
            throw new Exception("Tried to read a non-catalyst tag.");

        ItemStack output = buffer.readItemStack();
        recipe.setCatalyst(output);
    }

    public static void writeRecipeCatalysts(MiniaturizationRecipe recipe, PacketBuffer buffer) {
        ItemStack catalyst = recipe.getCatalyst();

        CompoundNBT outputMeta = new CompoundNBT();
        outputMeta.putString("type", TYPE_CATALYSTS.toString());
        buffer.writeCompoundTag(outputMeta);
        buffer.writeItemStack(catalyst);
    }
}
