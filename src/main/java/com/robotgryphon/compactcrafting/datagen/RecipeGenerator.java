package com.robotgryphon.compactcrafting.datagen;

import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(Registration.FIELD_PROJECTOR_ITEM.get(), 1)
                .requires(Registration.BASE_ITEM.get())
                .requires(Registration.PROJECTOR_DISH_ITEM.get())
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.BASE_ITEM.get(), 4)
                .pattern(" R ")
                .pattern("DSD")
                .pattern("PPP")
                .define('S', Items.STONE_SLAB)
                .define('R', Items.REDSTONE_TORCH)
                .define('D', Items.DIAMOND)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.PROJECTOR_DISH_ITEM.get(), 4)
                .pattern("GI ")
                .pattern("GEI")
                .pattern("GI ")
                .define('E', Items.ENDER_EYE)
                .define('G', Tags.Items.GLASS_PANES)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(Registration.MATCH_PROXY_ITEM.get())
                .requires(Registration.BASE_ITEM.get())
                .requires(Items.REDSTONE)
                .unlockedBy("got_redstone", has(Items.REDSTONE))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(Registration.RESCAN_PROXY_ITEM.get())
                .requires(Registration.BASE_ITEM.get())
                .requires(Items.CRAFTING_TABLE)
                .unlockedBy("got_crafting_table", has(Items.CRAFTING_TABLE))
                .save(consumer);
    }
}
