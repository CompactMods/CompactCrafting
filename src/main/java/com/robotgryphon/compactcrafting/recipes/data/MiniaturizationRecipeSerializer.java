package com.robotgryphon.compactcrafting.recipes.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class MiniaturizationRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, JsonObject json) {
        MiniaturizationRecipe r = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .result()
                .orElse(null);

        if (r != null) r.setId(recipeId);
        return r;
    }

    @Nullable
    @Override
    public MiniaturizationRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        CompactCrafting.LOGGER.debug("Starting recipe read: {}", recipeId);

        CompoundNBT n = buffer.readCompoundTag();
        if (n != null && n.contains("recipe")) {
            INBT recipeNbt = n.get("recipe");

            MiniaturizationRecipe rec = MiniaturizationRecipe.CODEC.decode(NBTDynamicOps.INSTANCE, recipeNbt)
                    .resultOrPartial(err -> {

                    }).get().getFirst();

            rec.setId(recipeId);
            return rec;
        }

        CompactCrafting.LOGGER.error(String.format("Miniaturization recipe failed to decode: %s", recipeId));
        return null;
    }

    @Override
    public void write(PacketBuffer buffer, MiniaturizationRecipe recipe) {
        NBTDynamicOps ops = NBTDynamicOps.INSTANCE;
        try {
            DataResult<INBT> encode = MiniaturizationRecipe.CODEC.encodeStart(ops, recipe);
            encode
                    .resultOrPartial(err -> {
                        CompactCrafting.LOGGER.error(String.format("Failed to write to packet for recipe: %s", recipe.getId()));
                        CompactCrafting.LOGGER.error(err);
                    })
                    .ifPresent(nbt -> {
                        CompoundNBT n = new CompoundNBT();
                        n.put("recipe", nbt);
                        buffer.writeCompoundTag(n);
                    });
        }

        catch(NullPointerException npe) {
            CompactCrafting.LOGGER.error(String.format("Whoops: %s", recipe.getId()), npe);
        }
    }

}
