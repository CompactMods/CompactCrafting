package dev.compactmods.crafting.recipes;

public record RecipeScanResult(
        net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate matchedBlocks,
        net.minecraft.world.item.crafting.RecipeHolder<MiniaturizationRecipe> recipe) {
}
