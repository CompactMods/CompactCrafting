package dev.compactmods.crafting.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.catalyst.CatalystMatcherCodec;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.components.RecipeComponentTypeCodec;
import dev.compactmods.crafting.recipes.layers.RecipeLayerTypeCodec;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.world.item.ItemStack;

public class MiniaturizationRecipeCodec {

    public static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerTypeCodec.INSTANCE.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    public static final Codec<IRecipeComponent> COMPONENT_CODEC =
            RecipeComponentTypeCodec.INSTANCE.dispatchStable(IRecipeComponent::getType, RecipeComponentType::getCodec);
}
