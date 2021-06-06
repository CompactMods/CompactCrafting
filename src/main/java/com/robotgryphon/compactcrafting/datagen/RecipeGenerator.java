package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(Registration.FIELD_PROJECTOR_ITEM.get(), 4)
                .pattern(" E ")
                .pattern(" R ")
                .pattern("DID")
                .define('E', Items.ENDER_EYE)
                .define('R', Items.REDSTONE_TORCH)
                .define('D', Items.DIAMOND)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);
    }
}
