package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.core.CCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends RecipeProvider {
    public static class Runner extends RecipeProvider.Runner {
        private final String name;

        public Runner(String name, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
            this.name = name;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new RecipeGenerator(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public RecipeGenerator(HolderLookup.Provider holders, RecipeOutput output) {
        super(holders, output);
    }



//        ShapelessRecipeBuilder.shapeless(CCItems.MATCH_PROXY_ITEM.get())
//                .requires(CCItems.BASE_ITEM.get())
//                .requires(Items.REDSTONE)
//                .unlockedBy("got_redstone", has(Items.REDSTONE))
//                .save(consumer);
//
//        ShapelessRecipeBuilder.shapeless(CCItems.RESCAN_PROXY_ITEM.get())
//                .requires(CCItems.BASE_ITEM.get())
//                .requires(Items.CRAFTING_TABLE)
//                .unlockedBy("got_crafting_table", has(Items.CRAFTING_TABLE))
//                .save(consumer);

    @Override
    protected void buildRecipes() {
        shapeless(RecipeCategory.MISC, CCItems.FIELD_PROJECTOR_ITEM.get(), 1)
                .requires(CCItems.BASE_ITEM.get())
                .requires(CCItems.PROJECTOR_DISH_ITEM.get())
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        shaped(RecipeCategory.MISC, CCItems.BASE_ITEM.get(), 4)
                .pattern(" R ")
                .pattern("DSD")
                .pattern("PPP")
                .define('S', Items.STONE_SLAB)
                .define('R', Items.REDSTONE_TORCH)
                .define('D', Items.DIAMOND)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        shaped(RecipeCategory.MISC, CCItems.PROJECTOR_DISH_ITEM.get(), 4)
                .pattern("GI ")
                .pattern("GEI")
                .pattern("GI ")
                .define('E', Items.ENDER_EYE)
                .define('G', Tags.Items.GLASS_PANES)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(output);
    }
}
